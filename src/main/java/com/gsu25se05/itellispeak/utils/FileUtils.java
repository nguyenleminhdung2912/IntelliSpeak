package com.gsu25se05.itellispeak.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils {
    public static String extractTextFromCV(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Tên file không hợp lệ.");
        }

        if (filename.toLowerCase().endsWith(".pdf")) {
            return extractTextFromPdf(file.getInputStream());
        } else if (filename.toLowerCase().endsWith(".docx")) {
            return extractTextFromDocx(file.getInputStream());
        } else {
            throw new IllegalArgumentException("Chỉ hỗ trợ file PDF hoặc DOCX.");
        }
    }

    private static String extractTextFromPdf(InputStream inputStream) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private static String extractTextFromDocx(InputStream inputStream) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            return paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));
        }
    }
}
