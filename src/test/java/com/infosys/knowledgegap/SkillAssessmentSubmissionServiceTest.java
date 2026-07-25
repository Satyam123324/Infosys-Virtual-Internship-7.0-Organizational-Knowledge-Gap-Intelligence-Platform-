package com.infosys.knowledgegap;

import com.infosys.knowledgegap.dto.SkillAssessmentSubmissionRequest;
import com.infosys.knowledgegap.entity.Role;
import com.infosys.knowledgegap.entity.Skill;
import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.enums.AssessmentType;
import com.infosys.knowledgegap.enums.RoleType;
import com.infosys.knowledgegap.repository.EmployeeProfileRepository;
import com.infosys.knowledgegap.repository.EmployeeSkillRepository;
import com.infosys.knowledgegap.repository.SkillAssessmentSubmissionRepository;
import com.infosys.knowledgegap.repository.SkillRepository;
import com.infosys.knowledgegap.repository.UserRepository;
import com.infosys.knowledgegap.service.impl.SkillAssessmentSubmissionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillAssessmentSubmissionServiceTest {

    @Mock SkillAssessmentSubmissionRepository submissionRepository;
    @Mock UserRepository userRepository;
    @Mock SkillRepository skillRepository;
    @Mock EmployeeProfileRepository employeeProfileRepository;
    @Mock EmployeeSkillRepository employeeSkillRepository;

    @InjectMocks SkillAssessmentSubmissionServiceImpl service;

    private User employee(long id, String email) {
        return User.builder()
                .id(id).email(email)
                .roles(Set.of(Role.builder().id(1L).name(RoleType.EMPLOYEE).build()))
                .build();
    }

    @Test
    void submit_peerReviewOfYourself_isRejected() {
        User me = employee(1L, "me@corp.com");
        when(userRepository.findByEmail("me@corp.com")).thenReturn(Optional.of(me));
        when(skillRepository.findById(10L)).thenReturn(Optional.of(Skill.builder().id(10L).build()));

        SkillAssessmentSubmissionRequest req = new SkillAssessmentSubmissionRequest();
        req.setSkillId(10L);
        req.setAssessedUserId(1L); // same person as the caller
        req.setType(AssessmentType.PEER);
        req.setRating(4);

        assertThrows(IllegalArgumentException.class, () -> service.submit("me@corp.com", req));
    }

    @Test
    void submit_managerAssessmentByNonManager_isRejected() {
        User me = employee(1L, "me@corp.com"); // only EMPLOYEE role
        when(userRepository.findByEmail("me@corp.com")).thenReturn(Optional.of(me));
        when(skillRepository.findById(10L)).thenReturn(Optional.of(Skill.builder().id(10L).build()));

        SkillAssessmentSubmissionRequest req = new SkillAssessmentSubmissionRequest();
        req.setSkillId(10L);
        req.setAssessedUserId(2L); // someone else
        req.setType(AssessmentType.MANAGER);
        req.setRating(5);

        assertThrows(IllegalArgumentException.class, () -> service.submit("me@corp.com", req));
    }
}
