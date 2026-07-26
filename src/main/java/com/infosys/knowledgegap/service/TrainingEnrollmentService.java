package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.LearningMilestoneResponse;
import com.infosys.knowledgegap.dto.TeamMemberLearningProgressResponse;
import com.infosys.knowledgegap.dto.TrainingEnrollmentRequest;
import com.infosys.knowledgegap.dto.TrainingEnrollmentResponse;

import java.util.List;

public interface TrainingEnrollmentService {

    TrainingEnrollmentResponse enroll(String email, TrainingEnrollmentRequest request);

    List<TrainingEnrollmentResponse> getMyEnrollments(String email);

    /** My learning milestones (achievements) newest-first — powers learning-velocity analytics. */
    List<LearningMilestoneResponse> getMyMilestones(String email);

    TrainingEnrollmentResponse updateProgress(String email, Long enrollmentId, int progressPercent);

    void cancelEnrollment(String email, Long enrollmentId);

    /** Org-wide per-person training progress summary — access is role-gated at the controller. */
    List<TeamMemberLearningProgressResponse> getTeamProgress();
}
