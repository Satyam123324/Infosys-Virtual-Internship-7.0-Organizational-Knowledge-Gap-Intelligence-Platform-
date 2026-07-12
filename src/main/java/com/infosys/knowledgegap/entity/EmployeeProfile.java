package com.infosys.knowledgegap.entity;

import com.infosys.knowledgegap.enums.EmploymentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employee_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(length = 100)
    private String currentRoleTitle;

    @Column(length = 500)
    private String resumeUrl;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    private LocalDate dateOfJoining;

    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EmployeeSkill> skills = new HashSet<>();

    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Certification> certifications = new HashSet<>();

    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<WorkExperience> workExperience = new HashSet<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
