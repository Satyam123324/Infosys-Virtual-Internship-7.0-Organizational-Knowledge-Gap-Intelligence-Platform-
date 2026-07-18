package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.PeerAssessmentRequest;
import com.infosys.knowledgegap.dto.PeerAssessmentResponse;
import com.infosys.knowledgegap.entity.*;
import com.infosys.knowledgegap.enums.ProficiencyLevel;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.*;
import com.infosys.knowledgegap.service.PeerAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PeerAssessmentServiceImpl implements PeerAssessmentService {

    private final PeerAssessmentRepository peerAssessmentRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final EmployeeSkillRepository employeeSkillRepository;

    private static final List<ProficiencyLevel> LEVEL_ORDER = List.of(
            ProficiencyLevel.UNAWARE, ProficiencyLevel.BEGINNER, ProficiencyLevel.INTERMEDIATE,
            ProficiencyLevel.ADVANCED, ProficiencyLevel.EXPERT
    );

    @Override
    public PeerAssessmentResponse submitPeerAssessment(String raterEmail, PeerAssessmentRequest request) {
        User rater = userRepository.findByEmail(raterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Rater not found"));
        User target = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Target employee not found"));

        if (rater.getId().equals(target.getId())) {
            throw new IllegalArgumentException("You cannot submit a peer assessment for yourself");
        }

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        PeerAssessment assessment = PeerAssessment.builder()
                .rater(rater)
                .target(target)
                .skill(skill)
                .ratedLevel(request.getRatedLevel())
                .comment(request.getComment())
                .build();
        assessment = peerAssessmentRepository.save(assessment);

        updateTargetSkillWithPeerRating(target, skill);

        return toResponse(assessment);
    }

    private void updateTargetSkillWithPeerRating(User target, Skill skill) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(target.getId())
                .orElseGet(() -> employeeProfileRepository.save(EmployeeProfile.builder().user(target).build()));

        List<PeerAssessment> allPeerRatings = peerAssessmentRepository.findByTargetIdOrderByCreatedAtDesc(target.getId())
                .stream().filter(pa -> pa.getSkill().getId().equals(skill.getId())).collect(Collectors.toList());

        double avgIndex = allPeerRatings.stream()
                .mapToInt(pa -> LEVEL_ORDER.indexOf(pa.getRatedLevel()))
                .average().orElse(0);
        int roundedPeerRating = (int) Math.round(avgIndex) + 1;

        EmployeeSkill employeeSkill = employeeSkillRepository
                .findByEmployeeProfileIdAndSkillId(profile.getId(), skill.getId())
                .orElse(EmployeeSkill.builder()
                        .employeeProfile(profile)
                        .skill(skill)
                        .proficiencyLevel(ProficiencyLevel.UNAWARE)
                        .build());

        employeeSkill.setPeerRating(roundedPeerRating);
        employeeSkillRepository.save(employeeSkill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeerAssessmentResponse> getAssessmentsReceivedByMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return peerAssessmentRepository.findByTargetIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeerAssessmentResponse> getAssessmentsGivenByMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return peerAssessmentRepository.findByRaterIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private PeerAssessmentResponse toResponse(PeerAssessment pa) {
        return PeerAssessmentResponse.builder()
                .id(pa.getId())
                .raterUserId(pa.getRater().getId())
                .raterName(pa.getRater().getFullName())
                .targetUserId(pa.getTarget().getId())
                .targetName(pa.getTarget().getFullName())
                .skillId(pa.getSkill().getId())
                .skillName(pa.getSkill().getName())
                .ratedLevel(pa.getRatedLevel())
                .comment(pa.getComment())
                .createdAt(pa.getCreatedAt())
                .build();
    }
}
