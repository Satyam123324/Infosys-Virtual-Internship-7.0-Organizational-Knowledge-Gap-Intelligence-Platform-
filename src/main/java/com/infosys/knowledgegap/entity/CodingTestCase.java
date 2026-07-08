package com.infosys.knowledgegap.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coding_test_cases")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodingTestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private CodingProblem problem;

    @Column(length = 1000)
    private String stdin;

    @Column(nullable = false, length = 1000)
    private String expectedOutput;

    @Builder.Default
    private boolean hidden = false; // hidden test cases are run but not shown to the user
}
