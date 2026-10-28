package com.hireforge.resume_service.service;

import com.hireforge.resume_service.dto.ResumeAnalysisResponse;
import com.hireforge.resume_service.entity.ResumeAnalysis;
import com.hireforge.resume_service.repository.ResumeAnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private FileParserService fileParserService;

    @Mock
    private GeminiService geminiService;

    @Mock
    private ResumeAnalysisRepository repository;

    @InjectMocks
    private ResumeService resumeService;

    private MockMultipartFile createFile() {
        return new MockMultipartFile("file", "resume.pdf", "application/pdf", "dummy".getBytes());
    }

    @Test
    void analyzeResume_ShouldSaveAndReturnResponse_WhenGeminiReturnsBracketedScore() throws IOException {
        MockMultipartFile file = createFile();
        String geminiText = "## ATS Score: [78]/100\n\nGood resume.";
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 28, 10, 0);

        ResumeAnalysis saved = new ResumeAnalysis();
        saved.setId(1L);
        saved.setUserId(5L);
        saved.setFileName("resume.pdf");
        saved.setAtsScore(78);
        saved.setGeminiResponse(geminiText);
        saved.setCreatedAt(createdAt);

        when(fileParserService.extractText(file)).thenReturn("resume text");
        when(geminiService.analyzeResume("resume text")).thenReturn(geminiText);
        when(repository.save(any(ResumeAnalysis.class))).thenReturn(saved);

        ResumeAnalysisResponse response = resumeService.analyzeResume(file, 5L);

        assertEquals(1L, response.getId());
        assertEquals("resume.pdf", response.getFileName());
        assertEquals(78, response.getAtsScore());
        assertEquals(geminiText, response.getGeminiResponse());
        assertEquals(createdAt, response.getCreatedAt());

        ArgumentCaptor<ResumeAnalysis> captor = ArgumentCaptor.forClass(ResumeAnalysis.class);
        verify(repository).save(captor.capture());
        ResumeAnalysis passedToRepository = captor.getValue();

        assertEquals(5L, passedToRepository.getUserId());
        assertEquals("resume.pdf", passedToRepository.getFileName());
        assertEquals(78, passedToRepository.getAtsScore());
        assertEquals(geminiText, passedToRepository.getGeminiResponse());
    }

    @Test
    void analyzeResume_ShouldExtractScore_WhenScoreHasNoBrackets() throws IOException {
        MockMultipartFile file = createFile();

        when(fileParserService.extractText(file)).thenReturn("resume text");
        when(geminiService.analyzeResume("resume text")).thenReturn("## ATS Score: 85/100");
        when(repository.save(any(ResumeAnalysis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResumeAnalysisResponse response = resumeService.analyzeResume(file, 5L);

        assertEquals(85, response.getAtsScore());
    }

    @Test
    void analyzeResume_ShouldSetScoreToZero_WhenGeminiResponseHasNoScore() throws IOException {
        MockMultipartFile file = createFile();

        when(fileParserService.extractText(file)).thenReturn("resume text");
        when(geminiService.analyzeResume("resume text")).thenReturn("Something unexpected from AI");
        when(repository.save(any(ResumeAnalysis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResumeAnalysisResponse response = resumeService.analyzeResume(file, 5L);

        assertEquals(0, response.getAtsScore());
    }

    @Test
    void analyzeResume_ShouldNotCallGeminiOrSave_WhenParsingFails() throws IOException {
        MockMultipartFile file = createFile();

        when(fileParserService.extractText(file))
                .thenThrow(new IllegalArgumentException("Only PDF and DOCX files are supported"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            resumeService.analyzeResume(file, 5L);
        });

        assertEquals("Only PDF and DOCX files are supported", exception.getMessage());
        verifyNoInteractions(geminiService);
        verifyNoInteractions(repository);
    }

    @Test
    void getAnalysisByUser_ShouldReturnMappedList_WhenUserHasAnalyses() {
        ResumeAnalysis first = new ResumeAnalysis();
        first.setId(1L);
        first.setUserId(5L);
        first.setFileName("resume1.pdf");
        first.setAtsScore(70);

        ResumeAnalysis second = new ResumeAnalysis();
        second.setId(2L);
        second.setUserId(5L);
        second.setFileName("resume2.docx");
        second.setAtsScore(90);

        when(repository.findByUserId(5L)).thenReturn(List.of(first, second));

        List<ResumeAnalysisResponse> result = resumeService.getAnalysisByUser(5L);

        assertEquals(2, result.size());
        assertEquals("resume1.pdf", result.get(0).getFileName());
        assertEquals(70, result.get(0).getAtsScore());
        assertEquals("resume2.docx", result.get(1).getFileName());
        assertEquals(90, result.get(1).getAtsScore());
    }

    @Test
    void getAnalysisByUser_ShouldReturnEmptyList_WhenUserHasNoAnalyses() {
        when(repository.findByUserId(99L)).thenReturn(List.of());

        List<ResumeAnalysisResponse> result = resumeService.getAnalysisByUser(99L);

        assertTrue(result.isEmpty());
    }
}