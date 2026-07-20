package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.ExpertProfileResponse;
import com.infosys.knowledgegap.dto.MentorshipSessionRequest;
import com.infosys.knowledgegap.dto.MentorshipSessionResponse;
import com.infosys.knowledgegap.enums.SessionStatus;

import java.util.List;

public interface MentorshipService {

    /** skillName is optional — null/blank browses top experts across all skills. */
    List<ExpertProfileResponse> findExperts(String email, String skillName);

    MentorshipSessionResponse bookSession(String email, MentorshipSessionRequest request);

    List<MentorshipSessionResponse> getMySessions(String email);

    MentorshipSessionResponse updateSessionStatus(String email, Long sessionId, SessionStatus newStatus);
}
