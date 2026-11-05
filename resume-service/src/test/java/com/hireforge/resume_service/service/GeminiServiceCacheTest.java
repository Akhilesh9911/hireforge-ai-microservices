package com.hireforge.resume_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest
@ActiveProfiles("test")
class GeminiServiceCacheTest {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {

        cacheManager.getCache("resumeAnalysis").clear();

        mockServer = MockRestServiceServer
                .bindTo(restTemplate)
                .build();
    }

    @Test
    void whenSameFileHash_GeminiCalledOnlyOnce() {

        String resumeText = "Java Developer with Spring Boot experience";
        String fileHash = "abc123";

        String response = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "## ATS Score: 85/100"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "generativelanguage.googleapis.com"
                        )
                ))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        String firstResult =
                geminiService.analyzeResume(resumeText, fileHash);

        String secondResult =
                geminiService.analyzeResume(resumeText, fileHash);

        assertEquals(firstResult, secondResult);

        // Only one HTTP request should happen
        mockServer.verify();
    }

    @Test
    void whenDifferentFileHash_GeminiCalledTwice() {

        String resumeText = "Java Developer with Spring Boot experience";

        String response = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "## ATS Score: 85/100"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        mockServer.expect(
                        org.springframework.test.web.client.ExpectedCount.twice(),
                        requestTo(
                                org.hamcrest.Matchers.containsString(
                                        "generativelanguage.googleapis.com"
                                )
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        geminiService.analyzeResume(resumeText, "hash-111");
        geminiService.analyzeResume(resumeText, "hash-222");

        mockServer.verify();
    }
}