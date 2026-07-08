package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.DepartmentGapSummary;
import com.infosys.knowledgegap.dto.EmployeeGapReport;

import java.util.List;

public interface GapAnalysisService {
    EmployeeGapReport getMyGapReport(String email);
    EmployeeGapReport getGapReportForUser(Long userId);
    List<EmployeeGapReport> getGapReportsForDepartment(Long departmentId);
    List<EmployeeGapReport> getAllGapReports();
    List<DepartmentGapSummary> getDepartmentGapSummaries();
}
