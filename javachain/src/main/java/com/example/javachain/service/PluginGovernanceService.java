package com.example.javachain.service;

import com.example.javachain.plugin.PluginMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * 插件治理服务。
 *
 * <p>负责插件注册表的维护（列表 / 启用 / 禁用）、语义化版本比较与兼容性判定，
 * 以及从插件包中读取元信息（{@code META-INF/plugin.json} 或 {@code MANIFEST.MF}）。
 *
 * <p><b>职责边界</b>：本服务只做「插件元数据登记 + 治理状态管理」，不对插件包做
 * 类加载。真正把插件暴露为 MCP 工具需要独立的类加载器与工具注册流程，当前不在
 * 本服务范围内；因此 {@link #loadPlugin(String)} 的语义是「登记插件包元信息」，
 * 而不是「动态加载插件类」。
 */
@Service
public class PluginGovernanceService {

    private static final Logger log = LoggerFactory.getLogger(PluginGovernanceService.class);

    /** 插件包内约定的元信息描述文件 */
    private static final String DESCRIPTOR_ENTRY = "META-INF/plugin.json";
    private static final String DESCRIPTOR_ENTRY_FALLBACK = "plugin.json";

    private static final String[] SUPPORTED_SUFFIXES = {".jar", ".zip"};

    private final ObjectMapper objectMapper;
    private final Map<String, PluginMetadata> plugins = new LinkedHashMap<>();

    @Value("${mcp.plugin-dir:./plugins}")
    private String pluginDir;

    public PluginGovernanceService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 列出已登记的插件。
     *
     * @param includeDisabled 是否包含已禁用的插件
     */
    public synchronized List<PluginMetadata> list(boolean includeDisabled) {
        List<PluginMetadata> all = new ArrayList<>(plugins.values());
        if (includeDisabled) {
            return all;
        }
        List<PluginMetadata> enabled = new ArrayList<>();
        for (PluginMetadata plugin : all) {
            if (plugin.getStatus() != PluginMetadata.PluginStatus.DISABLED) {
                enabled.add(plugin);
            }
        }
        return enabled;
    }

    public synchronized Optional<PluginMetadata> find(String pluginId) {
        if (pluginId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(plugins.get(pluginId));
    }

    /**
     * 启用插件。
     *
     * @return 启用后的插件元信息
     * @throws IllegalArgumentException 插件未登记
     */
    public synchronized PluginMetadata enable(String pluginId) {
        PluginMetadata plugin = require(pluginId);
        plugin.setStatus(PluginMetadata.PluginStatus.ACTIVE);
        plugin.setLastUpdated(LocalDateTime.now());
        log.info("Plugin enabled: {}", pluginId);
        return plugin;
    }

    /**
     * 禁用插件。
     *
     * @return 禁用后的插件元信息
     * @throws IllegalArgumentException 插件未登记
     */
    public synchronized PluginMetadata disable(String pluginId) {
        PluginMetadata plugin = require(pluginId);
        plugin.setStatus(PluginMetadata.PluginStatus.DISABLED);
        plugin.setLastUpdated(LocalDateTime.now());
        log.info("Plugin disabled: {}", pluginId);
        return plugin;
    }

    /**
     * 判定插件是否满足某个目标版本。
     *
     * <p>判定规则：插件自身版本 **不低于** 目标版本即视为兼容。
     *
     * @return 包含 pluginId / requestedVersion / pluginVersion / comparison / compatible / reason 的说明
     * @throws IllegalArgumentException 插件未登记
     */
    public synchronized Map<String, Object> checkCompatibility(String pluginId, String requestedVersion) {
        PluginMetadata plugin = require(pluginId);
        String pluginVersion = plugin.getVersion();

        int cmp = compareVersion(pluginVersion, requestedVersion);
        boolean compatible = cmp >= 0;

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("pluginId", pluginId);
        detail.put("pluginName", plugin.getName());
        detail.put("pluginVersion", pluginVersion);
        detail.put("requestedVersion", requestedVersion);
        detail.put("comparison", symbolOf(compareVersion(pluginVersion, requestedVersion)));
        detail.put("compatible", compatible);
        detail.put("reason", compatible
                ? "插件版本 " + pluginVersion + " 满足目标版本 " + requestedVersion
                : "插件版本 " + pluginVersion + " 低于目标版本 " + requestedVersion);
        return detail;
    }

    /**
     * 比较两个版本号。
     *
     * @return 包含 v1 / v2 / comparison（&gt; = &lt;）/ result（-1 / 0 / 1）的说明
     */
    public Map<String, Object> compareVersions(String v1, String v2) {
        int cmp = compareVersion(v1, v2);

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("v1", v1);
        detail.put("v2", v2);
        detail.put("comparison", symbolOf(cmp));
        detail.put("result", cmp);
        return detail;
    }

    /**
     * 登记一个插件包（不进行类加载）。
     *
     * @param jarPath 插件包路径，支持绝对路径；相对路径基于 {@code mcp.plugin-dir} 解析
     * @return 登记后的插件元信息
     * @throws IOException 文件不存在、类型不支持或元信息不可读
     */
    public synchronized PluginMetadata loadPlugin(String jarPath) throws IOException {
        File jar = resolvePluginFile(jarPath);

        Map<String, Object> descriptor = readDescriptor(jar);
        Manifest manifest = readManifest(jar);

        String id = firstNonBlank(
                asText(descriptor, "id"),
                manifestValue(manifest, Attributes.Name.IMPLEMENTATION_TITLE),
                baseName(jar)
        );

        PluginMetadata plugin = PluginMetadata.builder()
                .id(id)
                .name(firstNonBlank(asText(descriptor, "name"), id))
                .version(firstNonBlank(
                        asText(descriptor, "version"),
                        manifestValue(manifest, Attributes.Name.IMPLEMENTATION_VERSION),
                        "unknown"))
                .description(firstNonBlank(
                        asText(descriptor, "description"),
                        manifestValue(manifest, Attributes.Name.IMPLEMENTATION_VENDOR),
                        ""))
                .author(firstNonBlank(asText(descriptor, "author"),
                        manifestValue(manifest, Attributes.Name.IMPLEMENTATION_VENDOR), ""))
                .category(firstNonBlank(asText(descriptor, "category"), "MCP"))
                .jarPath(jar.getAbsolutePath())
                .checksum(sha256(jar))
                .status(PluginMetadata.PluginStatus.ACTIVE)
                .loadedAt(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .serverNames(readServerNames(descriptor, manifest))
                .dependencies(new ArrayList<>())
                .properties(readProperties(descriptor))
                .autoStart(asBoolean(descriptor, "autoStart", false))
                .priority(asInt(descriptor, "priority", 0))
                .build();

        plugins.put(plugin.getId(), plugin);
        log.info("Plugin registered: {} ({}) from {}", plugin.getId(), plugin.getVersion(), jar.getAbsolutePath());
        return plugin;
    }

    /**
     * 从注册表移除插件。
     *
     * @return true 表示确实移除了一个已登记的插件
     */
    public synchronized boolean unregister(String pluginId) {
        return plugins.remove(pluginId) != null;
    }

    private PluginMetadata require(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            throw new IllegalArgumentException("pluginId 不能为空");
        }
        PluginMetadata plugin = plugins.get(pluginId);
        if (plugin == null) {
            throw new IllegalArgumentException("插件未登记: " + pluginId + "，已登记: " + plugins.keySet());
        }
        return plugin;
    }

    /**
     * 解析插件包路径，并阻止相对路径越出插件目录。
     */
    private File resolvePluginFile(String jarPath) throws IOException {
        if (jarPath == null || jarPath.isBlank()) {
            throw new IOException("jarPath 不能为空");
        }

        File file = new File(jarPath.trim());
        if (!file.isAbsolute()) {
            File baseDir = new File(pluginDir).getCanonicalFile();
            file = new File(baseDir, jarPath.trim()).getCanonicalFile();
            String basePath = baseDir.getPath();
            if (!file.getPath().equals(basePath) && !file.getPath().startsWith(basePath + File.separator)) {
                throw new IOException("插件路径不能超出插件目录 " + basePath);
            }
        }

        if (!file.exists() || !file.isFile()) {
            throw new IOException("插件文件不存在: " + file.getAbsolutePath());
        }

        String lowerName = file.getName().toLowerCase(Locale.ROOT);
        boolean supported = false;
        for (String suffix : SUPPORTED_SUFFIXES) {
            if (lowerName.endsWith(suffix)) {
                supported = true;
                break;
            }
        }
        if (!supported) {
            throw new IOException("仅支持 .jar / .zip 插件包: " + file.getName());
        }

        return file;
    }

    private Map<String, Object> readDescriptor(File jar) {
        try (JarFile jarFile = new JarFile(jar)) {
            JarEntry entry = jarFile.getJarEntry(DESCRIPTOR_ENTRY);
            if (entry == null) {
                entry = jarFile.getJarEntry(DESCRIPTOR_ENTRY_FALLBACK);
            }
            if (entry == null) {
                return Map.of();
            }
            try (InputStream is = jarFile.getInputStream(entry)) {
                JsonNode node = objectMapper.readTree(is);
                if (node == null || !node.isObject()) {
                    return Map.of();
                }
                return objectMapper.convertValue(node, Map.class);
            }
        } catch (Exception e) {
            log.warn("读取插件描述文件失败 {}: {}", jar.getName(), e.getMessage());
            return Map.of();
        }
    }

    private Manifest readManifest(File jar) {
        try (JarFile jarFile = new JarFile(jar)) {
            return jarFile.getManifest();
        } catch (Exception e) {
            log.warn("读取插件 MANIFEST 失败 {}: {}", jar.getName(), e.getMessage());
            return null;
        }
    }

    private List<String> readServerNames(Map<String, Object> descriptor, Manifest manifest) {
        List<String> names = new ArrayList<>();

        Object servers = descriptor.get("serverNames");
        if (servers instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && !item.toString().isBlank()) {
                    names.add(item.toString().trim());
                }
            }
        }

        String manifestServers = manifestValue(manifest, new Attributes.Name("Mcp-Servers"));
        if (manifestServers != null) {
            for (String part : manifestServers.split(",")) {
                if (!part.isBlank() && !names.contains(part.trim())) {
                    names.add(part.trim());
                }
            }
        }

        return names;
    }

    private Map<String, String> readProperties(Map<String, Object> descriptor) {
        Map<String, String> properties = new HashMap<>();
        Object raw = descriptor.get("properties");
        if (raw instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    properties.put(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        }
        return properties;
    }

    private String sha256(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(file.toPath())) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : digest.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("计算插件校验和失败 {}: {}", file.getName(), e.getMessage());
            return null;
        }
    }

    private String manifestValue(Manifest manifest, Attributes.Name name) {
        if (manifest == null) {
            return null;
        }
        return manifest.getMainAttributes().getValue(name);
    }

    private String baseName(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private String asText(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    private boolean asBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean b) {
            return b;
        }
        if (value != null) {
            return Boolean.parseBoolean(value.toString());
        }
        return defaultValue;
    }

    private int asInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(value.toString().trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String firstNonBlank(String... candidates) {        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return null;
    }

    private String symbolOf(int cmp) {
        if (cmp > 0) {
            return ">";
        }
        if (cmp < 0) {
            return "<";
        }
        return "=";
    }

    /**
     * 语义化版本比较：按 {@code . - +} 切分逐段比较，数字段按数值比较，
     * 数字段优先于非数字段（因此 {@code 1.0 > 1.0-alpha}），缺失段按 0 补齐。
     *
     * @return 负数表示 left &lt; right，0 表示相等，正数表示 left &gt; right
     */
    public static int compareVersion(String left, String right) {
        String[] l = normalizeVersion(left);
        String[] r = normalizeVersion(right);

        int length = Math.max(l.length, r.length);
        for (int i = 0; i < length; i++) {
            String lv = i < l.length ? l[i] : "0";
            String rv = i < r.length ? r[i] : "0";

            Integer ln = parseNumber(lv);
            Integer rn = parseNumber(rv);

            int cmp;
            if (ln != null && rn != null) {
                cmp = Integer.compare(ln, rn);
            } else if (ln != null) {
                cmp = 1;
            } else if (rn != null) {
                cmp = -1;
            } else {
                cmp = lv.compareToIgnoreCase(rv);
            }

            if (cmp != 0) {
                return cmp < 0 ? -1 : 1;
            }
        }
        return 0;
    }

    private static String[] normalizeVersion(String version) {
        if (version == null || version.isBlank()) {
            return new String[]{"0"};
        }
        String trimmed = version.trim();
        if (trimmed.startsWith("v") || trimmed.startsWith("V")) {
            trimmed = trimmed.substring(1);
        }
        return trimmed.split("[.\\-+]");
    }

    private static Integer parseNumber(String segment) {
        try {
            return Integer.valueOf(segment);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
