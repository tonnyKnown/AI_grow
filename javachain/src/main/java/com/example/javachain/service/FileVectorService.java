package com.example.javachain.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 文件向量化服务 - 读取文件并向量化存储到知识库
 * 支持: .md, .txt, .json, .pdf, .png, .jpg, .jpeg, .gif, .bmp, .webp
 */
@Service
public class FileVectorService {

    private final RagService ragService;
    private final DocumentLoaderService documentLoaderService;
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public FileVectorService(RagService ragService, DocumentLoaderService documentLoaderService) {
        this.ragService = ragService;
        this.documentLoaderService = documentLoaderService;
    }

    /**
     * 加载指定目录下的所有文件并向量化（支持 PDF 和图片）
     * @param directoryPath 目录路径（相对于 resources/file/）
     * @return 加载结果
     */
    public String loadFilesFromDirectory(String directoryPath) {
        List<String> loadedFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        try {
            // 优先使用 PathMatchingResourcePatternResolver 扫描 classpath
            Resource[] resources = resolver.getResources("classpath:file/*");
            
            if (resources.length > 0) {
                for (Resource resource : resources) {
                    try {
                        String fileName = resource.getFilename();
                        if (fileName != null) {
                            if (isSupportedTextFile(fileName)) {
                                // 文本文件
                                String content = readFromInputStream(resource.getInputStream());
                                ragService.loadTextAsDocument(content, fileName);
                                loadedFiles.add(fileName);
                            } else if (documentLoaderService.isPdfFile(fileName)) {
                                // PDF 文件
                                File tempFile = createTempFile(resource.getInputStream(), fileName);
                                try {
                                    List<String> chunks = documentLoaderService.loadAndChunk(tempFile);
                                    loadChunks(fileName, chunks);
                                    loadedFiles.add(fileName + " (PDF, " + chunks.size() + " chunks)");
                                } finally {
                                    tempFile.delete();
                                }
                            } else if (documentLoaderService.isImageFile(fileName)) {
                                // 图片文件
                                File tempFile = createTempFile(resource.getInputStream(), fileName);
                                try {
                                    List<String> chunks = documentLoaderService.loadAndChunk(tempFile);
                                    loadChunks(fileName, chunks);
                                    loadedFiles.add(fileName + " (Image, " + chunks.size() + " chunks)");
                                } catch (Exception e) {
                                    // OCR 可能不可用，跳过但记录
                                    failedFiles.add(fileName + " (OCR 不可用)");
                                } finally {
                                    tempFile.delete();
                                }
                            }
                        }
                    } catch (IOException e) {
                        if (resource.getFilename() != null) {
                            failedFiles.add(resource.getFilename());
                        }
                    }
                }
            } else {
                // 尝试从文件系统读取
                Path absPath = Paths.get("src/main/resources/file/", directoryPath);
                if (Files.exists(absPath)) {
                    try (Stream<Path> paths = Files.walk(absPath)) {
                        paths.filter(Files::isRegularFile)
                             .forEach(path -> {
                                 try {
                                     String fileName = path.getFileName().toString();
                                     if (isSupportedTextFile(fileName)) {
                                         // 文本文件
                                         String content = Files.readString(path, StandardCharsets.UTF_8);
                                         ragService.loadTextAsDocument(content, fileName);
                                         loadedFiles.add(fileName);
                                     } else if (documentLoaderService.isPdfFile(fileName)) {
                                         // PDF 文件
                                         List<String> chunks = documentLoaderService.loadAndChunk(path.toString());
                                         loadChunks(fileName, chunks);
                                         loadedFiles.add(fileName + " (PDF, " + chunks.size() + " chunks)");
                                     } else if (documentLoaderService.isImageFile(fileName)) {
                                         // 图片文件
                                         try {
                                             List<String> chunks = documentLoaderService.loadAndChunk(path.toString());
                                             loadChunks(fileName, chunks);
                                             loadedFiles.add(fileName + " (Image, " + chunks.size() + " chunks)");
                                         } catch (Exception e) {
                                             failedFiles.add(fileName + " (OCR 不可用)");
                                         }
                                     }
                                 } catch (IOException e) {
                                     failedFiles.add(path.getFileName().toString());
                                 }
                             });
                    }
                } else {
                    return "目录不存在: " + absPath;
                }
            }

        } catch (IOException e) {
            return "读取目录失败: " + e.getMessage();
        }

        StringBuilder result = new StringBuilder();
        result.append("文件向量化完成！\n");
        result.append("成功加载: ").append(loadedFiles.size()).append(" 个文件\n");
        
        if (!loadedFiles.isEmpty()) {
            result.append("成功列表:\n");
            loadedFiles.forEach(file -> result.append("  - ").append(file).append("\n"));
        }
        
        if (!failedFiles.isEmpty()) {
            result.append("失败列表:\n");
            failedFiles.forEach(file -> result.append("  - ").append(file).append("\n"));
        }

        return result.toString();
    }

    /**
     * 加载单个文件（支持 PDF 和图片）
     * @param filePath 文件路径
     * @return 加载结果
     */
    public String loadSingleFile(String filePath) {
        try {
            // 检查文件扩展名
            String lowerPath = filePath.toLowerCase();
            
            if (documentLoaderService.isPdfFile(lowerPath)) {
                // PDF 文件
                return loadPdfFile(filePath);
            } else if (documentLoaderService.isImageFile(lowerPath)) {
                // 图片文件
                return loadImageFile(filePath);
            }
            
            // 文本文件处理
            // 先尝试从 classpath 读取
            ClassPathResource resource = new ClassPathResource("file/" + filePath);
            if (resource.exists()) {
                String content = readFromInputStream(resource.getInputStream());
                ragService.loadTextAsDocument(content, filePath);
                return "文件加载成功: " + filePath;
            }

            // 尝试从文件系统读取
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                String content = Files.readString(path, StandardCharsets.UTF_8);
                String fileName = path.getFileName().toString();
                ragService.loadTextAsDocument(content, fileName);
                return "文件加载成功: " + fileName;
            }

            // 尝试从 resources/file/ 目录读取
            Path resourcePath = Paths.get("src/main/resources/file/", filePath);
            if (Files.exists(resourcePath)) {
                String content = Files.readString(resourcePath, StandardCharsets.UTF_8);
                ragService.loadTextAsDocument(content, filePath);
                return "文件加载成功: " + filePath;
            }

            return "文件不存在: " + filePath;

        } catch (IOException e) {
            return "文件加载失败: " + e.getMessage();
        }
    }

    /**
     * 加载 PDF 文件
     */
    private String loadPdfFile(String filePath) {
        try {
            // 先尝试从 classpath
            ClassPathResource resource = new ClassPathResource("file/" + filePath);
            if (resource.exists()) {
                File tempFile = createTempFile(resource.getInputStream(), filePath);
                try {
                    List<String> chunks = documentLoaderService.loadAndChunk(tempFile);
                    loadChunks(filePath, chunks);
                    return "PDF 文件加载成功: " + filePath + " (" + chunks.size() + " chunks)";
                } finally {
                    tempFile.delete();
                }
            }

            // 尝试从文件系统
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                List<String> chunks = documentLoaderService.loadAndChunk(path.toString());
                loadChunks(filePath, chunks);
                return "PDF 文件加载成功: " + filePath + " (" + chunks.size() + " chunks)";
            }

            return "PDF 文件不存在: " + filePath;
        } catch (IOException e) {
            return "PDF 文件加载失败: " + e.getMessage();
        }
    }

    /**
     * 加载图片文件 (OCR)
     */
    private String loadImageFile(String filePath) {
        try {
            // 先尝试从 classpath
            ClassPathResource resource = new ClassPathResource("file/" + filePath);
            if (resource.exists()) {
                File tempFile = createTempFile(resource.getInputStream(), filePath);
                try {
                    List<String> chunks = documentLoaderService.loadAndChunk(tempFile);
                    loadChunks(filePath, chunks);
                    return "图片文件加载成功 (OCR): " + filePath + " (" + chunks.size() + " chunks)";
                } finally {
                    tempFile.delete();
                }
            }

            // 尝试从文件系统
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                List<String> chunks = documentLoaderService.loadAndChunk(path.toString());
                loadChunks(filePath, chunks);
                return "图片文件加载成功 (OCR): " + filePath + " (" + chunks.size() + " chunks)";
            }

            return "图片文件不存在: " + filePath;
        } catch (Exception e) {
            return "图片文件加载失败: " + e.getMessage() + " (请确保已安装 Tesseract OCR)";
        }
    }

    /**
     * 加载切分后的文本块
     */
    private void loadChunks(String fileName, List<String> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            String chunkName = fileName + "_chunk_" + i;
            ragService.loadTextAsDocument(chunks.get(i), chunkName);
        }
    }

    /**
     * 创建临时文件
     */
    private File createTempFile(InputStream inputStream, String fileName) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String ext = "";
        if (fileName.contains(".")) {
            ext = fileName.substring(fileName.lastIndexOf("."));
        }
        File tempFile = File.createTempFile("doc_", ext, new File(tempDir));
        Files.copy(inputStream, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    /**
     * 判断是否为支持的文本文件
     */
    private boolean isSupportedTextFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".md") || lower.endsWith(".txt") || lower.endsWith(".json");
    }

    /**
     * 从输入流读取内容
     */
    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * 获取文件目录下的所有文件名
     */
    public List<String> listFiles(String directoryPath) {
        List<String> files = new ArrayList<>();

        try {
            String pattern = "classpath:file/" + (directoryPath != null && !directoryPath.isEmpty() ? directoryPath + "/" : "") + "*";
            Resource[] resources = resolver.getResources(pattern);
            
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                if (fileName != null) {
                    files.add(fileName);
                }
            }
            
            if (files.isEmpty()) {
                Path absPath = Paths.get("src/main/resources/file/", directoryPath != null ? directoryPath : "");
                if (Files.exists(absPath)) {
                    try (Stream<Path> paths = Files.walk(absPath, 1)) {
                        paths.filter(Files::isRegularFile)
                             .forEach(path -> files.add(path.getFileName().toString()));
                    }
                }
            }
        } catch (IOException e) {
            // ignore
        }

        return files;
    }

    /**
     * 获取知识库统计信息
     */
    public String getKnowledgeBaseStats() {
        return "知识库文档数量: " + ragService.getKnowledgeBaseSize();
    }
}