package com.hireforge.interview_service.service;

import com.hireforge.interview_service.entity.InterviewSession;
import com.hireforge.interview_service.repository.InterviewSessionRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class InterviewService {

    private final GeminiService geminiService;
    private final InterviewSessionRepository repository;

    public InterviewService(GeminiService geminiService, InterviewSessionRepository repository) {
        this.geminiService = geminiService;
        this.repository = repository;
    }

    public InterviewSession generate(Long userId, String jobRole, MultipartFile file) throws IOException {
        String resumeText = extractText(file);
        String questions = geminiService.generateInterviewQuestions(jobRole, resumeText);

        InterviewSession session = new InterviewSession();
        session.setUserId(userId);
        session.setJobRole(jobRole);
        session.setQuestions(questions);

        return repository.save(session);
    }

    public List<InterviewSession> getHistory(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();

        if (filename != null && filename.endsWith(".pdf")) {
            PDDocument document = org.apache.pdfbox.Loader.loadPDF(file.getBytes());
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text;
        }

        if (filename != null && (filename.endsWith(".docx") || filename.endsWith(".doc"))) {
            XWPFDocument document = new XWPFDocument(file.getInputStream());
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
            document.close();
            return text.toString();
        }

        throw new RuntimeException("Unsupported file format. Please upload PDF or DOCX.");
    }
}