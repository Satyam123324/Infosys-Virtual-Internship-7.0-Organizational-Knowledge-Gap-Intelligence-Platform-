package com.infosys.knowledgegap.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.infosys.knowledgegap.dto.ResourceLink;
import com.infosys.knowledgegap.enums.ProficiencyLevel;
import com.infosys.knowledgegap.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Generates targeted, human-readable learning recommendations for each detected skill gap.
 *
 * Tries providers in this order, each purely optional:
 *   1. Google Gemini  — free tier available (no credit card), configured via GEMINI_API_KEY
 *   2. OpenAI          — paid, configured via OPENAI_API_KEY (kept as an alternative if preferred)
 *   3. Rule-based engine — always works, zero cost, zero external dependency
 *
 * If no key is configured, or a call fails for any reason (network issue, invalid key,
 * rate limit), this transparently falls back to the next option. The app never breaks
 * either way, and the caller (GapAnalysisService) doesn't need to know which path was used.
 */
@Service
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${app.openai.api-key:}")
    private String openaiApiKey;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String openaiModel;

    private static final Duration TIMEOUT = Duration.ofSeconds(12);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateRecommendationText(String skillName, ProficiencyLevel currentLevel,
                                              ProficiencyLevel requiredLevel, int gapSize, String severity) {
        String prompt = buildPrompt(skillName, currentLevel, requiredLevel, severity);

        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String aiText = callGemini(prompt.trim());
                if (aiText != null && !aiText.isBlank()) return aiText;
            } catch (Exception ex) {
                log.warn("Gemini recommendation call failed, trying next option: {}", ex.getMessage());
            }
        }

        if (openaiApiKey != null && !openaiApiKey.isBlank()) {
            try {
                String aiText = callOpenAi(prompt);
                if (aiText != null && !aiText.isBlank()) return aiText;
            } catch (Exception ex) {
                log.warn("OpenAI recommendation call failed, falling back to rule-based engine: {}", ex.getMessage());
            }
        }

        return ruleBasedRecommendation(skillName, currentLevel, requiredLevel, severity);
    }

    private String buildPrompt(String skillName, ProficiencyLevel currentLevel,
                                ProficiencyLevel requiredLevel, String severity) {
        String currentLabel = currentLevel != null ? formatLevel(currentLevel) : "no recorded proficiency";
        return String.format(
                "An employee's role requires %s proficiency in %s, but they are currently at %s " +
                "(gap severity: %s). Write ONE short, encouraging, actionable recommendation (2-3 sentences max) " +
                "on how they can close this specific skill gap. Be concrete and practical. Do not use markdown formatting.",
                formatLevel(requiredLevel), skillName, currentLabel, severity.toLowerCase());
    }

    // ---------- Google Gemini (free tier) ----------

    private String callGemini(String prompt) throws Exception {
        String trimmedKey = geminiApiKey.trim();
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel
                + ":generateContent?key=" + trimmedKey;

        ObjectNode partNode = objectMapper.createObjectNode();
        partNode.put("text", prompt);

        ObjectNode contentNode = objectMapper.createObjectNode();
        contentNode.putArray("parts").add(partNode);

        ObjectNode body = objectMapper.createObjectNode();
        body.putArray("contents").add(contentNode);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.warn("Gemini API returned status {}: {}", response.statusCode(), response.body());
            return null;
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode candidates = root.get("candidates");
        if (candidates != null && candidates.isArray() && candidates.size() > 0) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray() && parts.size() > 0 && parts.get(0).has("text")) {
                return parts.get(0).get("text").asText().trim();
            }
        }
        return null;
    }

    // ---------- OpenAI (paid, optional alternative) ----------

    private String callOpenAi(String prompt) throws Exception {
        ObjectNode systemMsg = objectMapper.createObjectNode();
        systemMsg.put("role", "system");
        systemMsg.put("content", "You are a helpful corporate Learning & Development advisor generating concise skill-gap recommendations for an internal HR platform.");

        ObjectNode userMsg = objectMapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);

        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(systemMsg);
        messages.add(userMsg);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", openaiModel);
        body.set("messages", messages);
        body.put("max_tokens", 150);
        body.put("temperature", 0.6);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openaiApiKey.trim())
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.warn("OpenAI API returned status {}: {}", response.statusCode(), response.body());
            return null;
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode choices = root.get("choices");
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode message = choices.get(0).get("message");
            if (message != null && message.has("content")) {
                return message.get("content").asText().trim();
            }
        }
        return null;
    }

    // ---------- Rule-based fallback (always available) ----------

    private String ruleBasedRecommendation(String skillName, ProficiencyLevel currentLevel,
                                            ProficiencyLevel requiredLevel, String severity) {
        String currentLabel = currentLevel != null ? formatLevel(currentLevel) : "no recorded proficiency";

        return switch (severity) {
            case "CRITICAL" -> String.format(
                    "%s is a critical gap — you're at %s but the role requires %s. " +
                    "Recommended: enroll in a structured %s course this quarter and take the built-in " +
                    "%s assessment test afterward to verify progress. Consider requesting a mentor or " +
                    "pairing session with a colleague who is Expert-level in %s.",
                    skillName, currentLabel, formatLevel(requiredLevel), skillName, skillName, skillName);
            case "MODERATE" -> String.format(
                    "%s needs focused attention — currently %s versus a required %s. " +
                    "Recommended: complete a targeted %s course or internal workshop over the next 4–6 weeks, " +
                    "then retake the %s assessment to confirm improvement.",
                    skillName, currentLabel, formatLevel(requiredLevel), skillName, skillName);
            case "MINOR" -> String.format(
                    "%s is close to the required level (%s vs. %s needed). " +
                    "Recommended: a short refresher course or hands-on practice project should close this gap quickly. " +
                    "Retake the %s assessment once ready to confirm the updated level.",
                    skillName, currentLabel, formatLevel(requiredLevel), skillName);
            default -> String.format("%s meets the required level for this role — no action needed.", skillName);
        };
    }

    @Override
    public List<ResourceLink> generateResourceLinks(String skillName, String severity) {
        String encoded = URLEncoder.encode(skillName, StandardCharsets.UTF_8);

        return List.of(
                ResourceLink.builder()
                        .title("Search " + skillName + " courses")
                        .provider("Coursera")
                        .url("https://www.coursera.org/search?query=" + encoded)
                        .build(),
                ResourceLink.builder()
                        .title("Search " + skillName + " courses")
                        .provider("Udemy")
                        .url("https://www.udemy.com/courses/search/?q=" + encoded)
                        .build(),
                ResourceLink.builder()
                        .title("Search " + skillName + " learning paths")
                        .provider("LinkedIn Learning")
                        .url("https://www.linkedin.com/learning/search?keywords=" + encoded)
                        .build()
        );
    }

    private String formatLevel(ProficiencyLevel level) {
        String name = level.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
