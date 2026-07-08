package com.infosys.knowledgegap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "role_competency_frameworks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoleCompetencyFramework {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false, length = 100)
    private String roleTitle;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String version = "v1";

    @Builder.Default
    private boolean current = true;

    @OneToMany(mappedBy = "framework", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CompetencyRequirement> requirements = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
