package com.infosys.knowledgegap;

import com.infosys.knowledgegap.dto.TrainingEnrollmentResponse;
import com.infosys.knowledgegap.entity.TrainingEnrollment;
import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.LearningMilestoneRepository;
import com.infosys.knowledgegap.repository.TrainingEnrollmentRepository;
import com.infosys.knowledgegap.repository.UserRepository;
import com.infosys.knowledgegap.service.NotificationService;
import com.infosys.knowledgegap.service.impl.TrainingEnrollmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingEnrollmentServiceTest {

    @Mock TrainingEnrollmentRepository trainingEnrollmentRepository;
    @Mock UserRepository userRepository;
    @Mock LearningMilestoneRepository learningMilestoneRepository;
    @Mock NotificationService notificationService;

    @InjectMocks TrainingEnrollmentServiceImpl service;

    @Test
    void updateProgress_onSomeoneElsesEnrollment_isRejected() {
        User caller = User.builder().id(1L).email("me@corp.com").build();
        User owner = User.builder().id(2L).build();
        TrainingEnrollment enrollment = TrainingEnrollment.builder()
                .id(5L).user(owner).courseName("Spring Boot").build();

        when(userRepository.findByEmail("me@corp.com")).thenReturn(Optional.of(caller));
        when(trainingEnrollmentRepository.findById(5L)).thenReturn(Optional.of(enrollment));

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateProgress("me@corp.com", 5L, 50));
    }

    @Test
    void updateProgress_byOwner_updatesPercent() {
        User owner = User.builder().id(1L).email("me@corp.com").build();
        TrainingEnrollment enrollment = TrainingEnrollment.builder()
                .id(5L).user(owner).courseName("Spring Boot").progressPercent(0).completed(false).build();

        when(userRepository.findByEmail("me@corp.com")).thenReturn(Optional.of(owner));
        when(trainingEnrollmentRepository.findById(5L)).thenReturn(Optional.of(enrollment));
        when(trainingEnrollmentRepository.save(any(TrainingEnrollment.class))).thenAnswer(i -> i.getArgument(0));

        TrainingEnrollmentResponse res = service.updateProgress("me@corp.com", 5L, 50);

        assertEquals(50, res.getProgressPercent());
    }
}
