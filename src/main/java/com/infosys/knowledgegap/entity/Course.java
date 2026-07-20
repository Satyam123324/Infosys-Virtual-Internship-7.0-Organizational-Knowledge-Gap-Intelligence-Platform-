package com.infosys.knowledgegap.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String provider;

    private String url;

    private String description;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    private Skill skill;
}