package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.learning.LearningProgressRequest;
import com.infosys.knowledgegap.dto.learning.LearningProgressResponse;

import java.util.List;

public interface LearningProgressService {

    LearningProgressResponse enrollCourse(LearningProgressRequest request);

    LearningProgressResponse updateProgress(Long id, Integer percentage);

    List<LearningProgressResponse> getEmployeeProgress(Long employeeId);

    List<LearningProgressResponse> getAllProgress();
}