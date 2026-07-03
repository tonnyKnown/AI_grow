package com.example.javachain.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文档加载服务 - 支持 PDF 和图片的文本提取
 * 图片 OCR 需要系统安装 Tesseract: https://github.com/UB-Mannheim/tesseract/wiki
 */
@Slf4j
@Service
public class DocumentLoaderService {

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;

    /**
     * 加载并切分文档（PDF 或图片）
     */
    public List<String> loadAndChunk(File file) throws IOException {
        String content = extractText(file);
        return chunkText(content);
    }

    /**
     * 从文件路径加载并切分文档
     */
    public List<String> loadAndChunk(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }
        return loadAndChunk(file);
    }

    /**
     * 从文件提取文本内容
     */
    public String extractText(File file) throws IOException {
        String lowerName = file.getName().toLowerCase();
        
        if (lowerName.endsWith(".pdf")) {
            return extractTextFromPdf(file);
        } else if (isImageFile(lowerName)) {
            return extractTextFromImage(file);
        } else {
            throw new IOException("不支持的文件格式: " + file.getName());
        }
    }

    /**
     * 从 PDF 文件提取文本
     */
    private String extractTextFromPdf(File file) throws IOException {
        log.info("从 PDF 提取文本: {}", file.getName());
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            log.info("PDF 文本提取完成，长度: {} 字符", text.length());
            return text;
        } catch (IOException e) {
            log.error("PDF 文本提取失败", e);
            throw new IOException("PDF 文本提取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从图片提取文本 (OCR)
     * 需要系统安装 Tesseract OCR
     */
    private String extractTextFromImage(File file) throws IOException {
        log.info("从图片提取文本 (OCR): {}", file.getName());
        
        String tessdataPath = getTessDataPath();
        File tessdataDir = new File(tessdataPath);
        
        if (!tessdataDir.exists()) {
            throw new IOException("Tesseract 数据目录不存在: " + tessdataPath + 
                    "\n请安装 Tesseract OCR: https://github.com/UB-Mannheim/tesseract/wiki");
        }
        
        File chiSimTrained = new File(tessdataPath, "chi_sim.traineddata");
        File engTrained = new File(tessdataPath, "eng.traineddata");
        
        if (!chiSimTrained.exists() && !engTrained.exists()) {
            throw new IOException("Tesseract 训练数据文件不存在。" +
                    "\n请下载 chi_sim.traineddata 和 eng.traineddata 到: " + tessdataPath);
        }
        
        try {
            String tessactCmd = findTesseract();
            String[] cmd = {
                tessactCmd,
                file.getAbsolutePath(),
                "stdout",
                "-l", "chi_sim+eng"
            };
            
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            StringBuilder result = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Tesseract OCR 执行失败，退出码: " + exitCode);
            }
            
            String text = result.toString().trim();
            log.info("OCR 文本提取完成，长度: {} 字符", text.length());
            return text;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("OCR 处理被中断", e);
        } catch (IOException e) {
            log.error("OCR 执行失败: {}", e.getMessage());
            throw new IOException("OCR 执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查找 Tesseract 可执行文件路径
     */
    private String findTesseract() {
        String path = System.getenv("TESSERACT_PATH");
        if (path != null && !path.isEmpty()) {
            return path;
        }
        
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return "tesseract";
        } else {
            return "/usr/bin/tesseract";
        }
    }

    /**
     * 获取 Tesseract 数据文件路径
     */
    private String getTessDataPath() {
        String tessdataEnv = System.getenv("TESSDATA_PREFIX");
        if (tessdataEnv != null && !tessdataEnv.isEmpty()) {
            return tessdataEnv;
        }
        
        String userHome = System.getProperty("user.home");
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return userHome + "\\AppData\\Local\\tesseract\\tessdata";
        } else {
            return userHome + "/tessdata";
        }
    }

    /**
     * 将文本按语义段落智能切分成块
     * 优先按段落边界切分，保持语义完整性
     */
    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        String cleaned = cleanText(text);
        
        // 按段落分割（连续空行作为段落边界）
        List<String> paragraphs = splitIntoParagraphs(cleaned);
        
        if (paragraphs.isEmpty()) {
            chunks.add(cleaned);
            return chunks;
        }

        // 合并段落，确保块大小合适且保持语义完整性
        StringBuilder currentChunk = new StringBuilder();
        
        for (int i = 0; i < paragraphs.size(); i++) {
            String paragraph = paragraphs.get(i).trim();
            
            if (paragraph.isEmpty()) {
                continue;
            }
            
            // 检查是否为标题（短行、可能以数字或特殊字符开头）
            boolean isHeading = isHeading(paragraph);
            
            // 如果当前块为空，直接添加
            if (currentChunk.length() == 0) {
                currentChunk.append(paragraph);
                // 如果是标题，后面紧跟内容
                if (isHeading && i + 1 < paragraphs.size()) {
                    String nextPara = paragraphs.get(i + 1).trim();
                    if (!nextPara.isEmpty() && !isHeading(nextPara)) {
                        currentChunk.append("\n\n").append(nextPara);
                        i++;
                    }
                }
            } else {
                // 计算添加新段落后的大小
                int newSize = currentChunk.length() + paragraph.length() + 2;
                
                if (newSize <= CHUNK_SIZE || isHeading) {
                    // 如果新段落能加入或当前是标题，合并到当前块
                    currentChunk.append("\n\n").append(paragraph);
                    
                    // 如果是标题，后面紧跟内容
                    if (isHeading && i + 1 < paragraphs.size()) {
                        String nextPara = paragraphs.get(i + 1).trim();
                        if (!nextPara.isEmpty() && !isHeading(nextPara)) {
                            int nextSize = currentChunk.length() + nextPara.length() + 2;
                            if (nextSize <= CHUNK_SIZE * 1.5) {
                                currentChunk.append("\n\n").append(nextPara);
                                i++;
                            }
                        }
                    }
                } else {
                    // 当前块已满，保存并开始新块
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder(paragraph);
                    
                    // 如果是标题，后面紧跟内容
                    if (isHeading && i + 1 < paragraphs.size()) {
                        String nextPara = paragraphs.get(i + 1).trim();
                        if (!nextPara.isEmpty() && !isHeading(nextPara)) {
                            currentChunk.append("\n\n").append(nextPara);
                            i++;
                        }
                    }
                }
            }
        }
        
        // 添加最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        log.info("文本切分完成，生成 {} 个块", chunks.size());
        return chunks;
    }

    /**
     * 将文本按段落分割（空行分隔）
     */
    private List<String> splitIntoParagraphs(String text) {
        List<String> paragraphs = new ArrayList<>();
        
        // 按多个换行符分割（考虑不同格式的段落分隔）
        String[] parts = text.split("\n{2,}");
        
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                paragraphs.add(trimmed);
            }
        }
        
        return paragraphs;
    }

    /**
     * 判断是否为标题
     */
    private boolean isHeading(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // 标题特征：
        // 1. 长度较短（通常少于50字符）
        if (text.length() > 100) {
            return false;
        }
        
        // 2. 以数字或序号开头（如 "1. " "第一章" "一、" "1)" "• "）
        if (text.matches("^([0-9]+[.、)])|([一二三四五六七八九十]+[.、])|(第[一二三四五六七八九十]+[章节部分条])|(•|\\*)\\s.*")) {
            return true;
        }
        
        // 3. 全大写或首字母大写（英文标题）
        if (text.matches("^[A-Z][a-z]*(\\s+[A-Z][a-z]*)*$") || text.equals(text.toUpperCase())) {
            return true;
        }
        
        // 4. 以特殊符号结尾（如冒号、问号）
        if (text.endsWith(":") || text.endsWith("？") || text.endsWith("?")) {
            return true;
        }
        
        return false;
    }

    /**
     * 清理文本
     */
    private String cleanText(String text) {
        return text
                .replaceAll("\r\n", "\n")
                .replaceAll("\r", "\n")
                .replaceAll("\t", " ")
                .replaceAll(" +", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    /**
     * 判断是否为图片文件
     */
    public boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".png") || 
               lower.endsWith(".jpg") || 
               lower.endsWith(".jpeg") || 
               lower.endsWith(".gif") || 
               lower.endsWith(".bmp") ||
               lower.endsWith(".webp");
    }

    /**
     * 判断是否为 PDF 文件
     */
    public boolean isPdfFile(String filename) {
        return filename.toLowerCase().endsWith(".pdf");
    }
}