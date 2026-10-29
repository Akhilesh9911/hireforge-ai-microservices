package com.hireforge.interview_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        geminiService = new GeminiService(restTemplate);
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-api-key");
    }

    private Map<String, Object> geminiResponse(String text) {
        return Map.of("candidates", List.of(
                Map.of("content", Map.of("parts", List.of(
                        Map.of("text", text))))));
    }

    @Test
    void generateInterviewQuestions_ShouldReturnText_WhenResponseIsValid() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(geminiResponse("Generated questions"));

        String result = geminiService.generateInterviewQuestions("Backend Developer", "resume text");

        assertEquals("Generated questions", result);
    }

    @Test
    void generateInterviewQuestions_ShouldSendApiKeyInUrlAndInputsInPrompt() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(geminiResponse("Generated questions"));

        geminiService.generateInterviewQuestions("Backend Developer", "Spring Boot resume text");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(restTemplate).postForObject(urlCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertTrue(urlCaptor.getValue().contains("generateContent"));
        assertTrue(urlCaptor.getValue().endsWith("?key=test-api-key"));

        String requestBody = bodyCaptor.getValue().toString();
        assertTrue(requestBody.contains("Backend Developer"));
        assertTrue(requestBody.contains("Spring Boot resume text"));
    }

    @Test
    void generateInterviewQuestions_ShouldThrowClearException_WhenCandidatesKeyIsMissing() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            geminiService.generateInterviewQuestions("Backend Developer", "resume text");
        });

        assertEquals("Gemini returned no candidates", exception.getMessage());
    }

    @Test
    void generateInterviewQuestions_ShouldThrowClearException_WhenCandidatesListIsEmpty() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("candidates", List.of()));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            geminiService.generateInterviewQuestions("Backend Developer", "resume text");
        });

        assertEquals("Gemini returned no candidates", exception.getMessage());
    }
}