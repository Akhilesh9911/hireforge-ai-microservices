package com.hireforge.resume_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResumeAnalysisResponse {
    private Long id;
    private String fileName;
    private Integer atsScore;
    private String geminiResponse;
    private LocalDateTime createdAt;
}