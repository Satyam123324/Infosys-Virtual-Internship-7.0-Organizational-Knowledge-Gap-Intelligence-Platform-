package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.ReviewableUserResponse;
import com.infosys.knowledgegap.dto.SkillAssessmentSubmissionRequest;
import com.infosys.knowledgegap.dto.SkillAssessmentSubmissionResponse;

import java.util.List;

public interface SkillAssessmentSubmissionService {

    SkillAssessmentSubmissionResponse submit(String email, SkillAssessmentSubmissionRequest request);

    /** Assessments received about the caller — from all three types. */
    List<SkillAssessmentSubmissionResponse> getReceivedByMe(String email);

    /** Assessments the caller has submitted about others (peer/manager history). */
    List<SkillAssessmentSubmissionResponse> getSubmittedByMe(String email);

    /** Everyone except the caller — for the peer/manager assessment "who am I rating" picker. */
    List<ReviewableUserResponse> getReviewableUsers(String email);
}
