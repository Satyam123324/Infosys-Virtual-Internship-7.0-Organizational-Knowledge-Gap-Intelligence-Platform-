package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.*;

import java.util.List;

public interface CodingChallengeService {
    List<CodingProblemDto> getProblemsByLanguage(String language);
    CodingProblemDto getProblem(Long problemId);
    CodingSubmissionResponse submitSolution(String email, CodingSubmitRequest request);
    List<CodingSubmissionResponse> getMySubmissions(String email);
}
