package com.hireforge.job_tracker_service.service;

import com.hireforge.job_tracker_service.dto.JobApplicationRequest;
import com.hireforge.job_tracker_service.dto.JobApplicationResponse;
import com.hireforge.job_tracker_service.entity.ApplicationStatus;
import com.hireforge.job_tracker_service.entity.JobApplication;
import com.hireforge.job_tracker_service.repository.JobApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository repository;

    @InjectMocks
    private JobApplicationService service;

    private JobApplication buildJob(Long id, Long userId) {
        JobApplication job = new JobApplication();
        job.setId(id);
        job.setUserId(userId);
        job.setCompanyName("Google");
        job.setJobTitle("Java Backend Developer");
        job.setStatus(ApplicationStatus.APPLIED);
        job.setAppliedDate(LocalDate.of(2026, 10, 3));
        job.setNotes("old notes");
        return job;
    }

    @Test
    void create_ShouldSaveJobWithUserIdFromHeader_AndReturnResponse() {
        JobApplicationRequest request = new JobApplicationRequest();
        request.setCompanyName("Google");
        request.setJobTitle("Java Backend Developer");
        request.setStatus(ApplicationStatus.APPLIED);
        request.setAppliedDate(LocalDate.of(2026, 10, 3));
        request.setNotes("Applied through LinkedIn");

        when(repository.save(any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication job = invocation.getArgument(0);
            job.setId(3L);
            return job;
        });

        JobApplicationResponse response = service.create(request, 5L);

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(repository).save(captor.capture());
        assertEquals(5L, captor.getValue().getUserId());
        assertEquals("Google", captor.getValue().getCompanyName());

        assertEquals(3L, response.getId());
        assertEquals("Google", response.getCompanyName());
        assertEquals("Java Backend Developer", response.getJobTitle());
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());
        assertEquals("Applied through LinkedIn", response.getNotes());
    }

    @Test
    void getAllByUser_ShouldReturnMappedList() {
        JobApplication first = buildJob(1L, 5L);
        JobApplication second = buildJob(2L, 5L);
        second.setCompanyName("Amazon");

        when(repository.findByUserId(5L)).thenReturn(List.of(first, second));

        List<JobApplicationResponse> result = service.getAllByUser(5L);

        assertEquals(2, result.size());
        assertEquals("Google", result.get(0).getCompanyName());
        assertEquals("Amazon", result.get(1).getCompanyName());
    }

    @Test
    void getById_ShouldReturnJob_WhenUserOwnsIt() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildJob(1L, 5L)));

        JobApplicationResponse response = service.getById(1L, 5L);

        assertEquals(1L, response.getId());
        assertEquals("Google", response.getCompanyName());
    }

    @Test
    void getById_ShouldThrowNotFound_WhenJobDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getById(99L, 5L);
        });

        assertEquals("Job application not found", exception.getMessage());
    }

    @Test
    void getById_ShouldThrowUnauthorized_WhenJobBelongsToAnotherUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildJob(1L, 5L)));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getById(1L, 99L);
        });

        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    void update_ShouldChangeOnlyProvidedFields_WhenRequestIsPartial() {
        JobApplication existing = buildJob(1L, 5L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(JobApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationRequest request = new JobApplicationRequest();
        request.setCompanyName("Alphabet");
        request.setNotes("new notes");

        JobApplicationResponse response = service.update(1L, request, 5L);

        assertEquals("Alphabet", response.getCompanyName());
        assertEquals("new notes", response.getNotes());
        assertEquals("Java Backend Developer", response.getJobTitle());
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());
        assertEquals(LocalDate.of(2026, 10, 3), response.getAppliedDate());
    }

    @Test
    void update_ShouldThrowUnauthorizedAndNotSave_WhenJobBelongsToAnotherUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildJob(1L, 5L)));

        JobApplicationRequest request = new JobApplicationRequest();
        request.setCompanyName("Hacked Company");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.update(1L, request, 99L);
        });

        assertEquals("Unauthorized", exception.getMessage());
        verify(repository, never()).save(any(JobApplication.class));
    }

    @Test
    void delete_ShouldDeleteJob_WhenUserOwnsIt() {
        JobApplication existing = buildJob(1L, 5L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L, 5L);

        verify(repository).delete(existing);
    }

    @Test
    void delete_ShouldThrowUnauthorizedAndNotDelete_WhenJobBelongsToAnotherUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildJob(1L, 5L)));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.delete(1L, 99L);
        });

        assertEquals("Unauthorized", exception.getMessage());
        verify(repository, never()).delete(any(JobApplication.class));
    }
}