package com.hireforge.interview_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent";

    private final RestTemplate restTemplate;

    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateInterviewQuestions(String jobRole, String resumeText) {
        String url = GEMINI_URL + "?key=" + apiKey;

        String prompt = """
                You are an expert technical interviewer with 10+ years of experience.
                Based on the candidate's resume and the job role below, generate exactly 10 interview questions.
                Return your response in EXACTLY this markdown format. Do NOT add any intro or conclusion text.

                ## Technical Questions

                1. **[Question]**
                   *Why asked:* [One line reason based on candidate's resume]

                2. **[Question]**
                   *Why asked:* [One line reason based on candidate's resume]

                3. **[Question]**
                   *Why asked:* [One line reason based on candidate's resume]

                4. **[Question]**
                   *Why asked:* [One line reason based on candidate's resume]

                5. **[Question]**
                   *Why asked:* [One line reason based on candidate's resume]

                6. **[Question]**
                   *Why asked:* [One line reason based on candidate's resume]

                ## Behavioral Questions

                7. **[Question]**
                   *Why asked:* [One line reason based on candidate's experience]

                8. **[Question]**
                   *Why asked:* [One line reason based on candidate's experience]

                9. **[Question]**
                   *Why asked:* [One line reason based on candidate's experience]

                10. **[Question]**
                    *Why asked:* [One line reason based on candidate's experience]

                ## Quick Tips for This Interview

                - [Tip specific to the job role]
                - [Tip specific to the candidate's background]
                - [General tip]

                ---
                Job Role: """ + jobRole + """

                Resume:
                """ + resumeText;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        Map response = restTemplate.postForObject(url, requestBody, Map.class);

        List<Map> candidates = (List<Map>) response.get("candidates");
        Map content = (Map) candidates.get(0).get("content");
        List<Map> parts = (List<Map>) content.get("parts");
        return (String) parts.get(0).get("text");
    }
}