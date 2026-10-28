package com.hireforge.resume_service.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FileParserServiceTest {

    private FileParserService fileParserService;

    @BeforeEach
    void setUp() {
        fileParserService = new FileParserService();
    }

    private byte[] createPdfBytes(String text) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(text);
                contentStream.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createDocxBytes(String firstLine, String secondLine) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            document.createParagraph().createRun().setText(firstLine);
            document.createParagraph().createRun().setText(secondLine);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Test
    void extractText_ShouldReturnText_WhenFileIsPdf() throws IOException {
        byte[] pdfBytes = createPdfBytes("Java Spring Boot Developer");
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", pdfBytes);

        String result = fileParserService.extractText(file);

        assertTrue(result.contains("Java Spring Boot Developer"));
    }

    @Test
    void extractText_ShouldReturnText_WhenFileIsDocx() throws IOException {
        byte[] docxBytes = createDocxBytes("Akhilesh Kumar", "Backend Developer");
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docxBytes);

        String result = fileParserService.extractText(file);

        assertTrue(result.contains("Akhilesh Kumar"));
        assertTrue(result.contains("Backend Developer"));
    }

    @Test
    void extractText_ShouldThrowException_WhenFileNameIsNull() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileParserService.extractText(file);
        });

        assertEquals("File name is null", exception.getMessage());
    }

    @Test
    void extractText_ShouldThrowException_WhenFileTypeIsUnsupported() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.txt", "text/plain", "plain text".getBytes());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileParserService.extractText(file);
        });

        assertEquals("Only PDF and DOCX files are supported", exception.getMessage());
    }

    @Test
    void extractText_ShouldThrowIOException_WhenPdfIsCorrupt() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "broken.pdf", "application/pdf", "this is not a real pdf".getBytes());

        assertThrows(IOException.class, () -> {
            fileParserService.extractText(file);
        });
    }
}