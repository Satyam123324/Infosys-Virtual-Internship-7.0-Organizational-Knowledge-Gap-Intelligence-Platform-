package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.ResourceLink;
import com.infosys.knowledgegap.enums.ProficiencyLevel;

import java.util.List;

public interface RecommendationService {
    String generateRecommendationText(String skillName, ProficiencyLevel currentLevel,
                                       ProficiencyLevel requiredLevel, int gapSize, String severity);
    List<ResourceLink> generateResourceLinks(String skillName, String severity);
}
