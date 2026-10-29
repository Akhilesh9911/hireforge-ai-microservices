package com.hireforge.interview_service.service;

import com.hireforge.interview_service.entity.InterviewSession;
import com.hireforge.interview_service.repository.InterviewSessionRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private InterviewSessionRepository repository;

    @InjectMocks
    private InterviewService interviewService;

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
    void generate_ShouldSendPdfTextToGeminiAndSaveSession_WhenFileIsPdf() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf",
                createPdfBytes("Java Spring Boot Developer"));

        when(geminiService.generateInterviewQuestions(eq("Backend Developer"), anyString()))
                .thenReturn("Q1, Q2, Q3");
        when(repository.save(any(InterviewSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InterviewSession result = interviewService.generate(5L, "Backend Developer", file);

        ArgumentCaptor<String> resumeTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(geminiService).generateInterviewQuestions(eq("Backend Developer"), resumeTextCaptor.capture());
        assertTrue(resumeTextCaptor.getValue().contains("Java Spring Boot Developer"));

        verify(repository).save(any(InterviewSession.class));
        assertEquals(5L, result.getUserId());
        assertEquals("Backend Developer", result.getJobRole());
        assertEquals("Q1, Q2, Q3", result.getQuestions());
    }

    @Test
    void generate_ShouldSendDocxTextToGeminiAndSaveSession_WhenFileIsDocx() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                createDocxBytes("Akhilesh Kumar", "Spring Boot Developer"));

        when(geminiService.generateInterviewQuestions(eq("Java Developer"), anyString()))
                .thenReturn("Docx questions");
        when(repository.save(any(InterviewSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InterviewSession result = interviewService.generate(7L, "Java Developer", file);

        ArgumentCaptor<String> resumeTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(geminiService).generateInterviewQuestions(eq("Java Developer"), resumeTextCaptor.capture());
        assertTrue(resumeTextCaptor.getValue().contains("Akhilesh Kumar"));
        assertTrue(resumeTextCaptor.getValue().contains("Spring Boot Developer"));

        assertEquals(7L, result.getUserId());
        assertEquals("Docx questions", result.getQuestions());
    }

    @Test
    void generate_ShouldThrowException_WhenFileTypeIsUnsupported() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.txt", "text/plain", "plain text".getBytes());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            interviewService.generate(5L, "Backend Developer", file);
        });

        assertEquals("Unsupported file format. Please upload PDF or DOCX.", exception.getMessage());
        verifyNoInteractions(geminiService);
        verifyNoInteractions(repository);
    }

    @Test
    void generate_ShouldThrowException_WhenFileNameIsNull() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            interviewService.generate(5L, "Backend Developer", file);
        });

        assertEquals("Unsupported file format. Please upload PDF or DOCX.", exception.getMessage());
        verifyNoInteractions(geminiService);
        verifyNoInteractions(repository);
    }

    @Test
    void generate_ShouldNotSaveSession_WhenGeminiFails() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf",
                createPdfBytes("Java Developer"));

        when(geminiService.generateInterviewQuestions(anyString(), anyString()))
                .thenThrow(new RuntimeException("Gemini unavailable"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            interviewService.generate(5L, "Backend Developer", file);
        });

        assertEquals("Gemini unavailable", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void generate_ShouldAcceptFile_WhenExtensionIsUpperCase() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "RESUME.PDF", "application/pdf",
                createPdfBytes("Uppercase Resume"));

        when(geminiService.generateInterviewQuestions(eq("Backend Developer"), anyString()))
                .thenReturn("Q1");
        when(repository.save(any(InterviewSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InterviewSession result = interviewService.generate(5L, "Backend Developer", file);

        ArgumentCaptor<String> resumeTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(geminiService).generateInterviewQuestions(eq("Backend Developer"), resumeTextCaptor.capture());
        assertTrue(resumeTextCaptor.getValue().contains("Uppercase Resume"));
        assertEquals("Q1", result.getQuestions());
    }
}