package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.AssessmentQuestionDto;
import com.infosys.knowledgegap.dto.AssessmentResultResponse;
import com.infosys.knowledgegap.dto.AssessmentSubmitRequest;

import java.util.List;

public interface AssessmentService {
    List<AssessmentQuestionDto> getQuestionsForSkill(Long skillId);
    AssessmentResultResponse submitAssessment(String email, AssessmentSubmitRequest request);
    List<AssessmentResultResponse> getMyResults(String email);
}
