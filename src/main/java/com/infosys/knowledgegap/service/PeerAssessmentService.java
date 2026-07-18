package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.PeerAssessmentRequest;
import com.infosys.knowledgegap.dto.PeerAssessmentResponse;

import java.util.List;

public interface PeerAssessmentService {
    PeerAssessmentResponse submitPeerAssessment(String raterEmail, PeerAssessmentRequest request);
    List<PeerAssessmentResponse> getAssessmentsReceivedByMe(String email);
    List<PeerAssessmentResponse> getAssessmentsGivenByMe(String email);
}
