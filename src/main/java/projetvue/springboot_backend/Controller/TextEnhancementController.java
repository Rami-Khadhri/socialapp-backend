// TextEnhancementController.java
package projetvue.springboot_backend.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api")
public class TextEnhancementController {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

    @PostMapping("/enhance")
    public Map<String, String> enhanceText(@RequestBody Map<String, String> request) {
        String inputText = request.get("text");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> prompt = new HashMap<>();
        prompt.put("text", "Enhance this text with more descriptive details in 2-3 sentences and insert atleast 1 emoji: " + inputText);

        requestBody.put("contents", new Object[]{
                Map.of("parts", new Object[]{prompt})
        });

        String url = GEMINI_API + "?key=" + apiKey;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map response = restTemplate.postForObject(
                    url,
                    entity,
                    Map.class
            );

            assert response != null;
            if (response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (!parts.isEmpty()) {
                        return Map.of("enhancedText", parts.get(0).get("text").toString());
                    }
                }
            }
            return Map.of("error", "No valid response from Gemini API");
        } catch (Exception e) {
            return Map.of("error", "Failed to enhance text: " + e.getMessage());
        }
    }


    @PostMapping("/ask")
    public Map askQuestion(@RequestBody Map<String, Object> request) {
        String question = (String) request.get("question");
        String responseLength = request.getOrDefault("responseLength", "default").toString();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, String> prompt = new HashMap<>();

        // Customize the prompt based on desired response length
        String promptText;
        switch (responseLength) {
            case "short":
                promptText = "Provide a brief, concise answer in 1-2 sentences to this question: " + question;
                break;
            case "long":
                promptText = "Provide a detailed, comprehensive answer with examples and explanations to this question: " + question;
                break;
            default:
                promptText = "Answer this question clearly and appropriately: " + question;
        }

        prompt.put("text", promptText);

        requestBody.put("contents", new Object[]{
                Map.of("parts", new Object[]{prompt})
        });

        String url = GEMINI_API + "?key=" + apiKey;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(
                    url,
                    entity,
                    Map.class
            );

            assert response != null;
            if (response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> content = (Map) firstCandidate.get("content");
                    List<Map<String, Object>> parts = (List) content.get("parts");
                    if (!parts.isEmpty()) {
                        return Map.of("answer", parts.get(0).get("text").toString());
                    }
                }
            }
            return Map.of("error", "No valid response from Gemini API");
        } catch (Exception e) {
            return Map.of("error", "Failed to get answer: " + e.getMessage());
        }
    }
}