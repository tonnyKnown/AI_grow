package com.example.javachain.controller;

import com.example.javachain.common.ApiResult;
import com.example.javachain.service.DocumentLoaderService;
import com.example.javachain.service.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文档控制器 - 处理 PDF 和图片上传与加载
 */
@Slf4j
@RestController
@RequestMapping("/api/document")
public class DocumentController {

    private final DocumentLoaderService documentLoaderService;
    private final RagService ragService;

    public DocumentController(DocumentLoaderService documentLoaderService, RagService ragService) {
        this.documentLoaderService = documentLoaderService;
        this.ragService = ragService;
    }

    /**
     * 上传 PDF 或图片文件并加载到知识库
     */
    @PostMapping("/upload")
    public ApiResult<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {
        
        String filename = file.getOriginalFilename();
        log.info("收到文档上传请求: {}, 大小: {} bytes", filename, file.getSize());
        
        try {
            if (file.isEmpty()) {
                return ApiResult.error(400, "文件不能为空", null);
            }
            
            File tempFile = saveToTemp(file);
            try {
                String docTitle = title != null && !title.isEmpty() ? title : filename;
                List<String> chunks = documentLoaderService.loadAndChunk(tempFile);
                
                int count = 0;
                for (String chunk : chunks) {
                    ragService.loadTextAsDocument(chunk, docTitle + "_chunk_" + count);
                    count++;
                }
                
                log.info("文档加载完成，切分 {} 个块", count);
                
                return ApiResult.success(Map.of(
                        "filename", filename,
                        "chunks", count,
                        "message", "文档加载成功"
                ));
            } finally {
                tempFile.delete();
            }
            
        } catch (IOException e) {
            log.error("文档加载失败", e);
            return ApiResult.error(500, "文档加载失败: " + e.getMessage(), null);
        }
    }

    /**
     * 从文件路径加载 PDF 或图片
     */
    @PostMapping("/load")
    public ApiResult<Map<String, Object>> loadDocument(@RequestBody Map<String, String> request) {
        String filePath = request.get("filePath");
        String title = request.getOrDefault("title", "document");
        
        log.info("收到文档加载请求: {}", filePath);
        
        try {
            List<String> chunks = documentLoaderService.loadAndChunk(filePath);
            
            int count = 0;
            for (String chunk : chunks) {
                ragService.loadTextAsDocument(chunk, title + "_chunk_" + count);
                count++;
            }
            
            log.info("文档加载完成，切分 {} 个块", count);
            
            return ApiResult.success(Map.of(
                    "filePath", filePath,
                    "chunks", count,
                    "message", "文档加载成功"
            ));
            
        } catch (IOException e) {
            log.error("文档加载失败", e);
            return ApiResult.error(500, "文档加载失败: " + e.getMessage(), null);
        }
    }

    /**
     * 提取 PDF 或图片文本（预览）
     */
    @PostMapping("/extract")
    public ApiResult<Map<String, Object>> extractText(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        log.info("收到文本提取请求: {}", filename);
        
        try {
            if (file.isEmpty()) {
                return ApiResult.error(400, "文件不能为空", null);
            }
            
            File tempFile = saveToTemp(file);
            try {
                String content = documentLoaderService.extractText(tempFile);
                List<String> chunks = documentLoaderService.loadAndChunk(tempFile);
                
                return ApiResult.success(Map.of(
                        "filename", filename,
                        "textLength", content.length(),
                        "chunks", chunks.size(),
                        "preview", content.length() > 500 ? content.substring(0, 500) + "..." : content
                ));
            } finally {
                tempFile.delete();
            }
            
        } catch (IOException e) {
            log.error("文本提取失败", e);
            return ApiResult.error(500, "文本提取失败: " + e.getMessage(), null);
        }
    }

    /**
     * 保存上传文件到临时目录
     */
    private File saveToTemp(MultipartFile file) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + ext;
        Path tempPath = Path.of(tempDir, filename);
        Files.copy(file.getInputStream(), tempPath);
        return tempPath.toFile();
    }

    /**
     * 获取支持的文档格式
     */
    @GetMapping("/supported-formats")
    public ApiResult<Map<String, Object>> getSupportedFormats() {
        return ApiResult.success(Map.of(
                "pdf", List.of("pdf"),
                "images", List.of("png", "jpg", "jpeg", "gif", "bmp", "webp"),
                "note", "图片文件需要 Tesseract OCR 支持中英文识别"
        ));
    }
}