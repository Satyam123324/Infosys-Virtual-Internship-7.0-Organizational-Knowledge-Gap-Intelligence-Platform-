package com.infosys.knowledgegap.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.infosys.knowledgegap.dto.CodeRunRequest;
import com.infosys.knowledgegap.dto.CodeRunResponse;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.service.CodeExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Executes user-submitted code via Piston (https://github.com/engineer-man/piston),
 * a free, publicly hosted, sandboxed code execution API. We deliberately do NOT run
 * arbitrary code as an OS process on our own server — that would be a real security
 * risk (arbitrary code execution) if this app were ever exposed beyond local dev.
 * Piston isolates each run in its own sandboxed container on their infrastructure.
 *
 * Note: this is a free public service intended for low-volume/educational use
 * (rate-limited to a few requests/second). Perfect for a student project demo;
 * not meant for production-scale traffic.
 */
@Service
@Slf4j
public class CodeExecutionServiceImpl implements CodeExecutionService {

    private static final String PISTON_URL = "https://emkc.org/api/v2/piston/execute";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Maps our friendly language names to Piston's expected language id + filename
    private static final Map<String, String[]> LANGUAGE_CONFIG = Map.of(
            "python", new String[]{"python", "main.py"},
            "java", new String[]{"java", "Main.java"},
            "javascript", new String[]{"javascript", "main.js"},
            "cpp", new String[]{"cpp", "main.cpp"},
            "go", new String[]{"go", "main.go"},
            "c", new String[]{"c", "main.c"}
    );

    @Override
    public CodeRunResponse execute(CodeRunRequest request) {
        String lang = request.getLanguage().toLowerCase();
        String[] config = LANGUAGE_CONFIG.get(lang);
        if (config == null) {
            throw new ResourceNotFoundException("Unsupported language: " + request.getLanguage()
                    + ". Supported: " + LANGUAGE_CONFIG.keySet());
        }

        try {
            ObjectNode fileNode = objectMapper.createObjectNode();
            fileNode.put("name", config[1]);
            fileNode.put("content", request.getCode());

            ObjectNode body = objectMapper.createObjectNode();
            body.put("language", config[0]);
            body.put("version", "*");
            body.putArray("files").add(fileNode);
            body.put("stdin", request.getStdin() != null ? request.getStdin() : "");

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(PISTON_URL))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Piston API returned status {}: {}", response.statusCode(), response.body());
                return CodeRunResponse.builder()
                        .stdout("")
                        .stderr("Code execution service returned an error (status " + response.statusCode()
                                + "). It may be rate-limited — please wait a moment and try again.")
                        .exitCode(-1)
                        .build();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode run = root.get("run");

            String stdout = run != null && run.has("stdout") ? run.get("stdout").asText() : "";
            String stderr = run != null && run.has("stderr") ? run.get("stderr").asText() : "";
            int exitCode = run != null && run.has("code") ? run.get("code").asInt() : -1;

            // Compile errors (e.g. Java/C++) show up under "compile" instead of "run"
            JsonNode compile = root.get("compile");
            if (compile != null && compile.has("stderr") && !compile.get("stderr").asText().isBlank()) {
                stderr = compile.get("stderr").asText() + (stderr.isBlank() ? "" : "\n" + stderr);
            }

            return CodeRunResponse.builder()
                    .stdout(stdout)
                    .stderr(stderr)
                    .exitCode(exitCode)
                    .build();

        } catch (Exception ex) {
            log.error("Code execution failed", ex);
            return CodeRunResponse.builder()
                    .stdout("")
                    .stderr("Failed to reach the code execution service. Check your internet connection and try again.")
                    .exitCode(-1)
                    .build();
        }
    }
}
