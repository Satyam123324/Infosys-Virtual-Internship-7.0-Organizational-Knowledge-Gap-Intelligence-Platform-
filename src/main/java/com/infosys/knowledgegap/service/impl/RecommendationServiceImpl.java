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
 * If an OpenAI API key is configured (OPENAI_API_KEY env var / app.openai.api-key), this
 * service calls the OpenAI Chat Completions API to generate a genuinely AI-written
 * recommendation tailored to the specific gap. If no key is configured, or the call fails
 * for any reason (network issue, invalid key, rate limit), it transparently falls back to
 * a deterministic rule-based recommendation — the app never breaks either way, and the
 * caller (GapAnalysisService) doesn't need to know which path was used.
 */
@Service
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    @Value("${app.openai.api-key:}")
    private String apiKey;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String model;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final Duration TIMEOUT = Duration.ofSeconds(12);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateRecommendationText(String skillName, ProficiencyLevel currentLevel,
                                              ProficiencyLevel requiredLevel, int gapSize, String severity) {
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String aiText = callOpenAi(skillName, currentLevel, requiredLevel, severity);
                if (aiText != null && !aiText.isBlank()) {
                    return aiText;
                }
            } catch (Exception ex) {
                log.warn("OpenAI recommendation call failed, falling back to rule-based engine: {}", ex.getMessage());
            }
        }
        return ruleBasedRecommendation(skillName, currentLevel, requiredLevel, severity);
    }

    private String callOpenAi(String skillName, ProficiencyLevel currentLevel,
                               ProficiencyLevel requiredLevel, String severity) throws Exception {
        String currentLabel = currentLevel != null ? formatLevel(currentLevel) : "no recorded proficiency";

        String prompt = String.format(
                "An employee's role requires %s proficiency in %s, but they are currently at %s " +
                "(gap severity: %s). Write ONE short, encouraging, actionable recommendation (2-3 sentences max) " +
                "on how they can close this specific skill gap. Be concrete and practical. Do not use markdown formatting.",
                formatLevel(requiredLevel), skillName, currentLabel, severity.toLowerCase());

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
        body.put("model", model);
        body.set("messages", messages);
        body.put("max_tokens", 150);
        body.put("temperature", 0.6);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_URL))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
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
