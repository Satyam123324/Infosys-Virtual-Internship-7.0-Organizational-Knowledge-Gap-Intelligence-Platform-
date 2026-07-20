package com.infosys.knowledgegap.dto.learning;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {

    private Long id;

    private String title;

    private String description;

    private String provider;

    private String url;

    private Long skillId;

    private String skillName;
}
