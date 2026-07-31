package com.hireforge.resume_service.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent";

    public String analyzeResume(String resumeText) {
        String prompt = buildPrompt(resumeText);
        String url = GEMINI_URL + "?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map candidates = (Map) ((List) response.getBody().get("candidates")).get(0);
        Map content = (Map) candidates.get("content");
        Map part = (Map) ((List) content.get("parts")).get(0);

        return (String) part.get("text");
    }

    private String buildPrompt(String resumeText) {
        return """
                You are an expert ATS (Applicant Tracking System) analyst and career coach.
                Analyze the resume below and return your response in EXACTLY this markdown format.
                Do NOT deviate from this structure. Do NOT add extra sections.

                ## ATS Score: [X]/100

                [One sentence explaining the score]

                ## Strengths

                1. **[Strength title]** — [Brief explanation]
                2. **[Strength title]** — [Brief explanation]
                3. **[Strength title]** — [Brief explanation]

                ## Areas for Improvement

                1. **[Area]** — [What to fix and why]
                2. **[Area]** — [What to fix and why]
                3. **[Area]** — [What to fix and why]

                ## Missing Skills (Based on Industry Standards)

                1. **[Skill]** — [Why it is important]
                2. **[Skill]** — [Why it is important]
                3. **[Skill]** — [Why it is important]
                4. **[Skill]** — [Why it is important]
                5. **[Skill]** — [Why it is important]

                ## Recommendations

                1. [Specific actionable recommendation]
                2. [Specific actionable recommendation]
                3. [Specific actionable recommendation]

                ---
                Resume:
                \"\"\"""" + resumeText + "\"\"\"";
    }

}