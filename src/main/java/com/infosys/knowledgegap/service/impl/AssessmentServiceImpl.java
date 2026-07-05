package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.*;
import com.infosys.knowledgegap.entity.*;
import com.infosys.knowledgegap.enums.AssessmentType;
import com.infosys.knowledgegap.enums.ProficiencyLevel;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.*;
import com.infosys.knowledgegap.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentQuestionRepository questionRepository;
    private final AssessmentResultRepository resultRepository;
    private final SkillRepository skillRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentQuestionDto> getQuestionsForSkill(Long skillId) {
        List<AssessmentQuestion> questions = questionRepository.findBySkillId(skillId);
        if (questions.isEmpty()) {
            throw new ResourceNotFoundException("No assessment questions available for this skill yet");
        }
        return questions.stream().map(q -> AssessmentQuestionDto.builder()
                .id(q.getId())
                .questionText(q.getQuestionText())
                .options(q.getOptions())
                .build()).collect(Collectors.toList());
    }

    @Override
    public AssessmentResultResponse submitAssessment(String email, AssessmentSubmitRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        EmployeeProfile profile = employeeProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> employeeProfileRepository.save(EmployeeProfile.builder().user(user).build()));

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        List<AssessmentQuestion> questions = questionRepository.findBySkillId(skill.getId());
        Map<Long, AssessmentQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(AssessmentQuestion::getId, q -> q));

        int correct = 0;
        for (AssessmentAnswerRequest answer : request.getAnswers()) {
            AssessmentQuestion question = questionMap.get(answer.getQuestionId());
            if (question != null && question.getCorrectOptionIndex().equals(answer.getSelectedOptionIndex())) {
                correct++;
            }
        }

        int total = request.getAnswers().size();
        double scorePercent = total == 0 ? 0.0 : (correct * 100.0) / total;
        ProficiencyLevel computedLevel = mapScoreToLevel(scorePercent);

        AssessmentResult result = AssessmentResult.builder()
                .employeeProfile(profile)
                .skill(skill)
                .totalQuestions(total)
                .correctAnswers(correct)
                .scorePercent(scorePercent)
                .computedLevel(computedLevel)
                .takenAt(LocalDateTime.now())
                .build();
        result = resultRepository.save(result);

        // Auto-update the employee's skill inventory with this system-verified result
        EmployeeSkill employeeSkill = employeeSkillRepository
                .findByEmployeeProfileIdAndSkillId(profile.getId(), skill.getId())
                .orElse(EmployeeSkill.builder().employeeProfile(profile).skill(skill).build());
        employeeSkill.setProficiencyLevel(computedLevel);
        employeeSkill.setAssessmentType(AssessmentType.SELF);
        employeeSkill.setLastAssessedDate(LocalDate.now());
        employeeSkill.setSelfRating((int) Math.round(scorePercent / 20.0)); // maps 0-100% to 0-5 scale
        employeeSkillRepository.save(employeeSkill);

        return toResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentResultResponse> getMyResults(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        EmployeeProfile profile = employeeProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));

        return resultRepository.findByEmployeeProfileIdOrderByTakenAtDesc(profile.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Maps assessment score percentage to a ProficiencyLevel.
     * 0-20%: Unaware | 21-40%: Beginner | 41-60%: Intermediate | 61-80%: Advanced | 81-100%: Expert
     */
    private ProficiencyLevel mapScoreToLevel(double scorePercent) {
        if (scorePercent <= 20) return ProficiencyLevel.UNAWARE;
        if (scorePercent <= 40) return ProficiencyLevel.BEGINNER;
        if (scorePercent <= 60) return ProficiencyLevel.INTERMEDIATE;
        if (scorePercent <= 80) return ProficiencyLevel.ADVANCED;
        return ProficiencyLevel.EXPERT;
    }

    private AssessmentResultResponse toResponse(AssessmentResult r) {
        return AssessmentResultResponse.builder()
                .id(r.getId())
                .skillId(r.getSkill().getId())
                .skillName(r.getSkill().getName())
                .totalQuestions(r.getTotalQuestions())
                .correctAnswers(r.getCorrectAnswers())
                .scorePercent(r.getScorePercent())
                .computedLevel(r.getComputedLevel())
                .takenAt(r.getTakenAt())
                .build();
    }
}
