package com.example.javachain.plugin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PluginMetadata {

    private String id;
    private String name;
    private String version;
    private String description;
    private String author;
    private String category;
    private String jarPath;
    private String checksum;
    private PluginStatus status;
    private LocalDateTime loadedAt;
    private LocalDateTime lastUpdated;
    private List<String> serverNames;
    private List<Dependency> dependencies;
    private Map<String, String> properties;
    private boolean autoStart;
    private int priority;

    public enum PluginStatus {
        PENDING,
        LOADING,
        ACTIVE,
        DISABLED,
        ERROR,
        UNLOADING
    }

    @Data
    @Builder
    public static class Dependency {
        private String pluginId;
        private String versionRange;
        private boolean optional;
    }
}
