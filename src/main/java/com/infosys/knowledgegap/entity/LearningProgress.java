package com.infosys.knowledgegap.entity;

import jakarta.persistence.*;
import lombok.*;
import com.infosys.knowledgegap.enums.ProgressStatus;
import java.time.LocalDate;

@Entity
@Table(name = "learning_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_profile_id")
    private EmployeeProfile employee;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Enumerated(EnumType.STRING)
    private ProgressStatus status;

    private Integer progressPercentage;

    private LocalDate enrolledDate;

    private LocalDate completedDate;
}   