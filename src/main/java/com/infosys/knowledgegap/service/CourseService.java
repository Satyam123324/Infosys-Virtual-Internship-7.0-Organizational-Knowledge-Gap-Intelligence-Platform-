package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.learning.CourseRequest;
import com.infosys.knowledgegap.dto.learning.CourseResponse;

import java.util.List;

public interface CourseService {

    CourseResponse createCourse(CourseRequest request);

    List<CourseResponse> getAllCourses();

    CourseResponse getCourseById(Long id);

    List<CourseResponse> getCoursesBySkill(Long skillId);

    CourseResponse updateCourse(Long id, CourseRequest request);

    void deleteCourse(Long id);
}