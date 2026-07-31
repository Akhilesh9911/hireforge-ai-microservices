package com.hireforge.job_tracker_service.dto;

import com.hireforge.job_tracker_service.entity.ApplicationStatus;
import java.time.LocalDate;

public class JobApplicationRequest {

    private String companyName;
    private String jobTitle;
    private ApplicationStatus status;
    private LocalDate appliedDate;
    private String notes;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public LocalDate getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}