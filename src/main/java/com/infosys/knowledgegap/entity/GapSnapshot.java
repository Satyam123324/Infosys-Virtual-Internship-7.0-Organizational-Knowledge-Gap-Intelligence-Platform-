package com.infosys.knowledgegap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * A daily point-in-time snapshot of org-wide gap health, so the Gap Analysis
 * module can chart trend-over-time (Module 4 — gap progression).
 */
@Entity
@Table(name = "gap_snapshots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GapSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate snapshotDate;

    private double avgReadinessPercent;
    private int totalGaps;
    private int criticalGaps;
    private int employeeCount;
}
