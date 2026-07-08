package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.RoleFrameworkRequest;
import com.infosys.knowledgegap.dto.RoleFrameworkResponse;

import java.util.List;

public interface CompetencyFrameworkService {
    RoleFrameworkResponse createOrUpdateFramework(RoleFrameworkRequest request);
    List<RoleFrameworkResponse> getAllFrameworks();
    RoleFrameworkResponse getFrameworkByRoleTitle(String roleTitle);
    RoleFrameworkResponse getFrameworkById(Long id);
    void deleteFramework(Long id);
}
