package com.hireforge.resume_service.service;

import com.hireforge.resume_service.dto.ResumeAnalysisResponse;
import com.hireforge.resume_service.entity.ResumeAnalysis;
import com.hireforge.resume_service.repository.ResumeAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeService {

    private final FileParserService fileParserService;
    private final GeminiService geminiService;
    private final ResumeAnalysisRepository repository;

    public ResumeService(FileParserService fileParserService, GeminiService geminiService, ResumeAnalysisRepository repository) {
        this.fileParserService = fileParserService;
        this.geminiService = geminiService;
        this.repository = repository;
    }

    public ResumeAnalysisResponse analyzeResume(MultipartFile file, Long userId) throws IOException {
        String extractedText = fileParserService.extractText(file);
        String geminiResponse = geminiService.analyzeResume(extractedText);
        Integer atsScore = extractAtsScore(geminiResponse);

        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setUserId(userId);
        analysis.setFileName(file.getOriginalFilename());
        analysis.setAtsScore(atsScore);
        analysis.setGeminiResponse(geminiResponse);

        ResumeAnalysis saved = repository.save(analysis);
        return mapToResponse(saved);
    }

    public List<ResumeAnalysisResponse> getAnalysisByUser(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Integer extractAtsScore(String geminiResponse) {
        Pattern pattern = Pattern.compile("ATS Score:\\s*\\[?(\\d+)\\]?/100");
        Matcher matcher = pattern.matcher(geminiResponse);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private ResumeAnalysisResponse mapToResponse(ResumeAnalysis analysis) {
        ResumeAnalysisResponse response = new ResumeAnalysisResponse();
        response.setId(analysis.getId());
        response.setFileName(analysis.getFileName());
        response.setAtsScore(analysis.getAtsScore());
        response.setGeminiResponse(analysis.getGeminiResponse());
        response.setCreatedAt(analysis.getCreatedAt());
        return response;
    }
}