package com.hiremate.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PdfTextExtractor {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Pattern EMPTY_PATTERN = Pattern.compile("\\s*");

    public static class ExtractResult {
        public final String text;
        public final boolean garbled;
        public final boolean isImagePdf;
        public final String filename;

        public ExtractResult(String text, boolean garbled, boolean isImagePdf, String filename) {
            this.text = text;
            this.garbled = garbled;
            this.isImagePdf = isImagePdf;
            this.filename = filename;
        }
    }

    public ExtractResult extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf")
                && !contentType.equals("application/octet-stream"))) {
            throw new IllegalArgumentException("仅支持 PDF 格式文件，当前格式: " + contentType);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("PDF文件大小不能超过10MB");
        }

        String filename = file.getOriginalFilename();

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String rawText = stripper.getText(document);

            if (EMPTY_PATTERN.matcher(rawText).matches()) {
                log.warn("PDF文字层为空，可能为扫描件: {}", filename);
                return new ExtractResult(null, false, true, filename);
            }

            GarbledResult garbled = detectGarbled(rawText);
            if (garbled.isGarbled) {
                log.warn("PDF文本可能存在乱码，文件: {}, 原因: {}", filename, garbled.reason);
                return new ExtractResult(rawText.trim(), true, false, filename);
            }

            log.info("PDF文本提取成功，文件: {}, 字符数: {}", filename, rawText.length());
            return new ExtractResult(rawText.trim(), false, false, filename);

        } catch (IOException e) {
            log.error("PDF解析失败: {}", e.getMessage(), e);
            throw new RuntimeException("PDF解析失败: " + e.getMessage(), e);
        }
    }

    private GarbledResult detectGarbled(String text) {
        if (text == null || text.isEmpty()) {
            return new GarbledResult(false, null);
        }

        int len = text.length();
        long replacementCount = 0;

        for (int i = 0; i < len; i++) {
            int cp = text.codePointAt(i);
            if (cp == 0x25A1 || cp == 0x25A0 || cp == 0xFFFD) {
                replacementCount++;
            }
        }

        if (len > 0 && (replacementCount * 100.0 / len > 5)) {
            return new GarbledResult(true, "替换字符占比过高");
        }

        long cjkCount = 0;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (isCjk(c)) cjkCount++;
        }

        if (replacementCount > 0 && len > 50) {
            double cjkRatio = cjkCount * 100.0 / len;
            if (cjkRatio < 1) {
                return new GarbledResult(true, "文本中CJK文字占比极低");
            }
        }

        return new GarbledResult(false, null);
    }

    private static boolean isCjk(char c) {
        return (c >= 0x4E00 && c <= 0x9FFF)
                || (c >= 0x3400 && c <= 0x4DBF)
                || (c >= 0x20000)
                || (c >= 0x3000 && c <= 0x303F)
                || (c >= 0xFF00 && c <= 0xFFEF);
    }

    private static class GarbledResult {
        final boolean isGarbled;
        final String reason;
        GarbledResult(boolean isGarbled, String reason) {
            this.isGarbled = isGarbled;
            this.reason = reason;
        }
    }

    public String cleanText(String rawText) {
        if (rawText == null) return "";
        return rawText
                .replaceAll("\\r\\n\\s*\\r\\n+", "\n\n")
                .replaceAll("[ \t]+", " ")
                .replaceAll("(?m)^[ \t]+", "")
                .trim();
    }
}
