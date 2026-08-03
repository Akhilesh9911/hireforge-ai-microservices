package com.hireforge.resume_service.dto;

import java.time.LocalDateTime;

public class ResumeAnalysisResponse {
    private Long id;
    private String fileName;
    private Integer atsScore;
    private String geminiResponse;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Integer getAtsScore() { return atsScore; }
    public void setAtsScore(Integer atsScore) { this.atsScore = atsScore; }

    public String getGeminiResponse() { return geminiResponse; }
    public void setGeminiResponse(String geminiResponse) { this.geminiResponse = geminiResponse; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}