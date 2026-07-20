package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.learning.CourseRequest;
import com.infosys.knowledgegap.dto.learning.CourseResponse;
import com.infosys.knowledgegap.entity.Course;
import com.infosys.knowledgegap.entity.Skill;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.CourseRepository;
import com.infosys.knowledgegap.repository.SkillRepository;
import com.infosys.knowledgegap.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final SkillRepository skillRepository;

   @Override
public CourseResponse createCourse(CourseRequest request) {

    Skill skill = skillRepository.findById(request.getSkillId())
            .orElseThrow(() ->
                    new ResourceNotFoundException("Skill not found with id: " + request.getSkillId()));

    Course course = new Course();
    course.setTitle(request.getTitle());
    course.setProvider(request.getProvider());
    course.setUrl(request.getUrl());
    course.setDescription(request.getDescription());
    course.setSkill(skill);

    Course savedCourse = courseRepository.save(course);

    return toResponse(savedCourse);
}

   @Override
@Transactional(readOnly = true)
public List<CourseResponse> getAllCourses() {

    return courseRepository.findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
}
@Override
@Transactional(readOnly = true)
public CourseResponse getCourseById(Long id) {

    Course course = courseRepository.findById(id)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Course not found with id: " + id));

    return toResponse(course);
}

    @Override
@Transactional(readOnly = true)
public List<CourseResponse> getCoursesBySkill(Long skillId) {

    skillRepository.findById(skillId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Skill not found with id: " + skillId));

    return courseRepository.findBySkillId(skillId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
}

    @Override
public CourseResponse updateCourse(Long id, CourseRequest request) {

    Course course = courseRepository.findById(id)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Course not found with id: " + id));

    Skill skill = skillRepository.findById(request.getSkillId())
            .orElseThrow(() ->
                    new ResourceNotFoundException("Skill not found with id: " + request.getSkillId()));

    course.setTitle(request.getTitle());
    course.setProvider(request.getProvider());
    course.setUrl(request.getUrl());
    course.setDescription(request.getDescription());
    course.setSkill(skill);

    Course updatedCourse = courseRepository.save(course);

    return toResponse(updatedCourse);
}

 @Override
public void deleteCourse(Long id) {

    Course course = courseRepository.findById(id)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Course not found with id: " + id));

    courseRepository.delete(course);
}

private CourseResponse toResponse(Course course) {

    CourseResponse response = new CourseResponse();

    response.setId(course.getId());
    response.setTitle(course.getTitle());
    response.setProvider(course.getProvider());
    response.setUrl(course.getUrl());
    response.setDescription(course.getDescription());

    if (course.getSkill() != null) {
        response.setSkillId(course.getSkill().getId());
        response.setSkillName(course.getSkill().getName());
    }

    return response;
}
}