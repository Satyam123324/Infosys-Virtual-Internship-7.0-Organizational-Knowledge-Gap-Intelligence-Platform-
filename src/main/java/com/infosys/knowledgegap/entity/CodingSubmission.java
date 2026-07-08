package com.infosys.knowledgegap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coding_submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_profile_id", nullable = false)
    private EmployeeProfile employeeProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private CodingProblem problem;

    @Column(length = 3000)
    private String submittedCode;

    @Column(nullable = false)
    private Integer totalTestCases;

    @Column(nullable = false)
    private Integer passedTestCases;

    @Column(nullable = false)
    private boolean allPassed;

    @Column(nullable = false)
    private LocalDateTime submittedAt;
}
