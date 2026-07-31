package com.hireforge.job_tracker_service.service;

import com.hireforge.job_tracker_service.dto.JobApplicationRequest;
import com.hireforge.job_tracker_service.dto.JobApplicationResponse;
import com.hireforge.job_tracker_service.entity.JobApplication;
import com.hireforge.job_tracker_service.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;

    public JobApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    public JobApplicationResponse create(JobApplicationRequest request, Long userId) {
        JobApplication job = new JobApplication();
        job.setUserId(userId);
        job.setCompanyName(request.getCompanyName());
        job.setJobTitle(request.getJobTitle());
        job.setStatus(request.getStatus());
        job.setAppliedDate(request.getAppliedDate());
        job.setNotes(request.getNotes());
        JobApplication saved = repository.save(job);
        return mapToResponse(saved);
    }

    public List<JobApplicationResponse> getAllByUser(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public JobApplicationResponse getById(Long id, Long userId) {
        JobApplication job = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job application not found"));
        if (!job.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return mapToResponse(job);
    }

    public JobApplicationResponse update(Long id, JobApplicationRequest request, Long userId) {
        JobApplication job = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job application not found"));
        if (!job.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (request.getCompanyName() != null) job.setCompanyName(request.getCompanyName());
        if (request.getJobTitle() != null) job.setJobTitle(request.getJobTitle());
        if (request.getStatus() != null) job.setStatus(request.getStatus());
        if (request.getAppliedDate() != null) job.setAppliedDate(request.getAppliedDate());
        if (request.getNotes() != null) job.setNotes(request.getNotes());
        JobApplication saved = repository.save(job);
        return mapToResponse(saved);
    }

    public void delete(Long id, Long userId) {
        JobApplication job = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job application not found"));
        if (!job.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        repository.delete(job);
    }

    private JobApplicationResponse mapToResponse(JobApplication job) {
        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(job.getId());
        response.setCompanyName(job.getCompanyName());
        response.setJobTitle(job.getJobTitle());
        response.setStatus(job.getStatus());
        response.setAppliedDate(job.getAppliedDate());
        response.setNotes(job.getNotes());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }
}