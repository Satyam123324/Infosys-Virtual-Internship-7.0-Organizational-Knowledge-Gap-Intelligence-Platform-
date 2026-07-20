package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.ExpertProfileResponse;
import com.infosys.knowledgegap.dto.MentorshipSessionRequest;
import com.infosys.knowledgegap.dto.MentorshipSessionResponse;
import com.infosys.knowledgegap.entity.EmployeeSkill;
import com.infosys.knowledgegap.entity.MentorshipSession;
import com.infosys.knowledgegap.entity.Skill;
import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.enums.ProficiencyLevel;
import com.infosys.knowledgegap.enums.SessionStatus;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.EmployeeSkillRepository;
import com.infosys.knowledgegap.repository.MentorshipSessionRepository;
import com.infosys.knowledgegap.repository.SkillRepository;
import com.infosys.knowledgegap.repository.UserRepository;
import com.infosys.knowledgegap.service.MentorshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MentorshipServiceImpl implements MentorshipService {

    // Only people genuinely strong in a skill show up as "experts" — self-ratings
    // of BEGINNER/INTERMEDIATE aren't useful for "find someone who knows X".
    private static final List<ProficiencyLevel> EXPERT_LEVELS = List.of(ProficiencyLevel.ADVANCED, ProficiencyLevel.EXPERT);
    private static final int BROWSE_LIMIT = 30;

    private final EmployeeSkillRepository employeeSkillRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final MentorshipSessionRepository mentorshipSessionRepository;

    @Override
    public List<ExpertProfileResponse> findExperts(String email, String skillName) {
        User currentUser = getUser(email);
        List<EmployeeSkill> matches;

        if (skillName != null && !skillName.isBlank()) {
            Skill skill = skillRepository.findByName(skillName.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("No skill found matching \"" + skillName + "\""));
            matches = employeeSkillRepository.findBySkillIdAndProficiencyLevelIn(skill.getId(), EXPERT_LEVELS);
        } else {
            matches = employeeSkillRepository.findByProficiencyLevelIn(EXPERT_LEVELS).stream()
                    .sorted(Comparator.comparing(EmployeeSkill::getProficiencyLevel).reversed())
                    .limit(BROWSE_LIMIT)
                    .collect(Collectors.toList());
        }

        return matches.stream()
                .filter(es -> es.getEmployeeProfile().getUser() != null
                        && !es.getEmployeeProfile().getUser().getId().equals(currentUser.getId()))
                .map(this::toExpertResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MentorshipSessionResponse bookSession(String email, MentorshipSessionRequest request) {
        User mentee = getUser(email);
        User mentor = userRepository.findById(request.getMentorId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        if (mentor.getId().equals(mentee.getId())) {
            throw new IllegalArgumentException("You cannot book a mentorship session with yourself");
        }

        MentorshipSession session = mentorshipSessionRepository.save(MentorshipSession.builder()
                .mentor(mentor)
                .mentee(mentee)
                .topic(request.getTopic())
                .scheduledAt(request.getScheduledAt())
                .status(SessionStatus.SCHEDULED)
                .build());

        return toSessionResponse(session, mentee.getId());
    }

    @Override
    public List<MentorshipSessionResponse> getMySessions(String email) {
        User user = getUser(email);
        return mentorshipSessionRepository.findByMentorIdOrMenteeIdOrderByScheduledAtAsc(user.getId(), user.getId())
                .stream()
                .map(s -> toSessionResponse(s, user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public MentorshipSessionResponse updateSessionStatus(String email, Long sessionId, SessionStatus newStatus) {
        User user = getUser(email);
        MentorshipSession session = mentorshipSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        boolean isParticipant = session.getMentor().getId().equals(user.getId())
                || session.getMentee().getId().equals(user.getId());
        if (!isParticipant) {
            throw new IllegalArgumentException("You are not a participant in this session");
        }
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new IllegalArgumentException("Only a scheduled session can be updated");
        }

        session.setStatus(newStatus);
        mentorshipSessionRepository.save(session);
        return toSessionResponse(session, user.getId());
    }

    // ---------- helpers ----------

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ExpertProfileResponse toExpertResponse(EmployeeSkill es) {
        User u = es.getEmployeeProfile().getUser();
        return ExpertProfileResponse.builder()
                .userId(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .department(u.getDepartment())
                .designation(u.getDesignation())
                .skillName(es.getSkill().getName())
                .proficiencyLevel(es.getProficiencyLevel())
                .build();
    }

    private MentorshipSessionResponse toSessionResponse(MentorshipSession s, Long viewerId) {
        return MentorshipSessionResponse.builder()
                .id(s.getId())
                .mentorId(s.getMentor().getId())
                .mentorName(s.getMentor().getFullName())
                .menteeId(s.getMentee().getId())
                .menteeName(s.getMentee().getFullName())
                .topic(s.getTopic())
                .scheduledAt(s.getScheduledAt())
                .status(s.getStatus())
                .notes(s.getNotes())
                .isMentor(s.getMentor().getId().equals(viewerId))
                .build();
    }
}
