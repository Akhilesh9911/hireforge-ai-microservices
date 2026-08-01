package com.hireforge.interview_service.controller;

import com.hireforge.interview_service.entity.InterviewSession;
import com.hireforge.interview_service.service.InterviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping(value = "/generate", consumes = "multipart/form-data")
    public ResponseEntity<InterviewSession> generate(
            @RequestHeader("X-User-Id") Long userId,
            @RequestPart("jobRole") String jobRole,
            @RequestPart("file") MultipartFile file) throws IOException {

        InterviewSession session = interviewService.generate(userId, jobRole, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @GetMapping("/history")
    public ResponseEntity<List<InterviewSession>> getHistory(
            @RequestHeader("X-User-Id") Long userId) {

        List<InterviewSession> sessions = interviewService.getHistory(userId);
        return ResponseEntity.ok(sessions);
    }
}