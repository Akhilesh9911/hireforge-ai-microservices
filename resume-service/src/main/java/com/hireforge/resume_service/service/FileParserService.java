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

        if (filename.endsWith(".pdf")) {
            return extractFromPdf(file);
        } else if (filename.endsWith(".docx")) {
            return extractFromDocx(file);
        } else {
            throw new IllegalArgumentException("Only PDF and DOCX files are supported");
        }
    }

    private String extractFromPdf(MultipartFile file) throws IOException {
        PDDocument document = Loader.loadPDF(file.getBytes());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();
        return text;
    }

    private String extractFromDocx(MultipartFile file) throws IOException {
        XWPFDocument document = new XWPFDocument(file.getInputStream());
        StringBuilder text = new StringBuilder();
        document.getParagraphs().forEach(paragraph -> {
            text.append(paragraph.getText()).append("\n");
        });
        document.close();
        return text.toString();
    }
}