package com.hireforge.resume_service.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class FileParserService {

    public String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException("File name is null");
        }

        String lowerCaseName = filename.toLowerCase();

        if (lowerCaseName.endsWith(".pdf")) {
            return extractFromPdf(file);
        } else if (lowerCaseName.endsWith(".docx")) {
            return extractFromDocx(file);
        } else {
            throw new IllegalArgumentException("Only PDF and DOCX files are supported");
        }
    }

    private String extractFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            StringBuilder text = new StringBuilder();
            document.getParagraphs().forEach(paragraph -> {
                text.append(paragraph.getText()).append("\n");
            });
            return text.toString();
        }
    }
}