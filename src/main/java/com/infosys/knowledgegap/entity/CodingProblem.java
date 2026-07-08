package com.infosys.knowledgegap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coding_problems")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodingProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 30)
    private String language; // python, java, javascript, cpp, go

    @Column(length = 100)
    private String difficulty; // Easy, Medium, Hard

    @Column(length = 3000)
    private String starterCode;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CodingTestCase> testCases = new ArrayList<>();
}
