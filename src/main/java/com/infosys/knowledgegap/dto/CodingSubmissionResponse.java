package com.infosys.knowledgegap.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodingSubmissionResponse {
    private Long id;
    private Long problemId;
    private String problemTitle;
    private Integer totalTestCases;
    private Integer passedTestCases;
    private boolean allPassed;
    private List<TestCaseResult> results;
    private LocalDateTime submittedAt;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TestCaseResult {
        private String stdin;
        private String expectedOutput;
        private String actualOutput;
        private boolean passed;
        private boolean hidden;
    }
}
