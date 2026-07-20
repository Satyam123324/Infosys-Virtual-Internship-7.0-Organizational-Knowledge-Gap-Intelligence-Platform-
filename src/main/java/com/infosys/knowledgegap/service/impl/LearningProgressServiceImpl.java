package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.learning.LearningProgressRequest;
import com.infosys.knowledgegap.dto.learning.LearningProgressResponse;
import com.infosys.knowledgegap.entity.Course;
import com.infosys.knowledgegap.entity.EmployeeProfile;
import com.infosys.knowledgegap.entity.LearningProgress;
import com.infosys.knowledgegap.enums.ProgressStatus;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.CourseRepository;
import com.infosys.knowledgegap.repository.EmployeeProfileRepository;
import com.infosys.knowledgegap.repository.LearningProgressRepository;
import com.infosys.knowledgegap.service.LearningProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningProgressServiceImpl implements LearningProgressService {

    private final LearningProgressRepository learningProgressRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final CourseRepository courseRepository;
  @Override
public LearningProgressResponse enrollCourse(LearningProgressRequest request) {

    EmployeeProfile employee = employeeProfileRepository.findById(request.getEmployeeId())
            .orElseThrow(() ->
                    new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

    Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() ->
                    new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

    LearningProgress progress = new LearningProgress();

    progress.setEmployee(employee);
    progress.setCourse(course);
    progress.setStatus(request.getStatus());
    progress.setProgressPercentage(request.getProgressPercentage());
    progress.setEnrolledDate(LocalDate.now());

    if (request.getStatus() == ProgressStatus.COMPLETED) {
        progress.setCompletedDate(LocalDate.now());
    }

    LearningProgress saved = learningProgressRepository.save(progress);

    return toResponse(saved);
}

@Override
public LearningProgressResponse updateProgress(Long id, Integer percentage) {

    LearningProgress progress = learningProgressRepository.findById(id)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Learning progress not found with id: " + id));

    progress.setProgressPercentage(percentage);

    if (percentage >= 100) {
        progress.setProgressPercentage(100);
        progress.setStatus(ProgressStatus.COMPLETED);
        progress.setCompletedDate(LocalDate.now());
    } else if (percentage > 0) {
        progress.setStatus(ProgressStatus.IN_PROGRESS);
    } else {
        progress.setStatus(ProgressStatus.NOT_STARTED);
    }

    LearningProgress updated = learningProgressRepository.save(progress);

    return toResponse(updated);
}

@Override
@Transactional(readOnly = true)
public List<LearningProgressResponse> getEmployeeProgress(Long employeeId) {

    employeeProfileRepository.findById(employeeId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Employee not found with id: " + employeeId));

    return learningProgressRepository.findByEmployeeId(employeeId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
}

@Override
@Transactional(readOnly = true)
public List<LearningProgressResponse> getAllProgress() {

    return learningProgressRepository.findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
}
private LearningProgressResponse toResponse(LearningProgress progress) {

    LearningProgressResponse response = new LearningProgressResponse();

    response.setId(progress.getId());

    response.setEmployeeId(progress.getEmployee().getId());
    response.setEmployeeName(
            progress.getEmployee().getUser().getFullName()
    );

    response.setCourseId(progress.getCourse().getId());
    response.setCourseTitle(progress.getCourse().getTitle());

    response.setStatus(progress.getStatus());
    response.setProgressPercentage(progress.getProgressPercentage());
    response.setEnrolledDate(progress.getEnrolledDate());
    response.setCompletedDate(progress.getCompletedDate());

    return response;
}
}