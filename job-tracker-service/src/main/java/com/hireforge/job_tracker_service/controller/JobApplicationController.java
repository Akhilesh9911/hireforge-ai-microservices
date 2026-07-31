package com.hireforge.job_tracker_service.controller;

import com.hireforge.job_tracker_service.dto.JobApplicationRequest;
import com.hireforge.job_tracker_service.dto.JobApplicationResponse;
import com.hireforge.job_tracker_service.service.JobApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(
            @RequestBody JobApplicationRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> getAll(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.getAllByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.getById(id, userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> update(
            @PathVariable Long id,
            @RequestBody JobApplicationRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.update(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}