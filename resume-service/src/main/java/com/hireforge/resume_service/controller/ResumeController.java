package com.hireforge.resume_service.controller;

import com.hireforge.resume_service.dto.ResumeAnalysisResponse;
import com.hireforge.resume_service.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId) throws IOException {

        ResumeAnalysisResponse response = resumeService.analyzeResume(file, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ResumeAnalysisResponse>> getHistory(
            @RequestHeader("X-User-Id") Long userId) {

        List<ResumeAnalysisResponse> history = resumeService.getAnalysisByUser(userId);
        return ResponseEntity.ok(history);
    }
}