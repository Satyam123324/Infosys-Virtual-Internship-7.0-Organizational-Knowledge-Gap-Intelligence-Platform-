package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.ProfileUpdateRequest;
import com.infosys.knowledgegap.dto.RoleUpdateRequest;
import com.infosys.knowledgegap.dto.UserProfileResponse;

import java.util.List;

public interface UserService {
    UserProfileResponse getMyProfile(String email);
    UserProfileResponse updateMyProfile(String email, ProfileUpdateRequest request);
    List<UserProfileResponse> getAllUsers();
    UserProfileResponse getUserById(Long id);
    UserProfileResponse updateUserRoles(Long userId, RoleUpdateRequest request);
    void toggleUserEnabled(Long userId, boolean enabled);
    void deleteUser(Long userId);
}
