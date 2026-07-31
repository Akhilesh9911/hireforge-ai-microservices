package com.hireforge.job_tracker_service.repository;

import com.hireforge.job_tracker_service.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUserId(Long userId);
}