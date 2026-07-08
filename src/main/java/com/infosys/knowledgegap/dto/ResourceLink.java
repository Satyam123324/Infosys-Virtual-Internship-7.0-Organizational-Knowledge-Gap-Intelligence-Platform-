package com.infosys.knowledgegap.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResourceLink {
    private String title;
    private String provider;
    private String url;
}
