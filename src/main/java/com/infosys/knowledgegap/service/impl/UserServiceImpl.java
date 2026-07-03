package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.ProfileUpdateRequest;
import com.infosys.knowledgegap.dto.RoleUpdateRequest;
import com.infosys.knowledgegap.dto.UserProfileResponse;
import com.infosys.knowledgegap.entity.Role;
import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.RoleRepository;
import com.infosys.knowledgegap.repository.UserRepository;
import com.infosys.knowledgegap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserProfileResponse getMyProfile(String email) {
        return mapToResponse(findByEmail(email));
    }

    @Override
    public UserProfileResponse updateMyProfile(String email, ProfileUpdateRequest request) {
        User user = findByEmail(email);
        if (request.getFullName() != null)        user.setFullName(request.getFullName());
        if (request.getDepartment() != null)      user.setDepartment(request.getDepartment());
        if (request.getDesignation() != null)     user.setDesignation(request.getDesignation());
        if (request.getProfileImageUrl() != null) user.setProfileImageUrl(request.getProfileImageUrl());
        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserById(Long id) {
        return mapToResponse(findById(id));
    }

    @Override
    public UserProfileResponse updateUserRoles(Long userId, RoleUpdateRequest request) {
        User user = findById(userId);
        Set<Role> newRoles = new HashSet<>();
        request.getRoles().forEach(roleType -> {
            Role role = roleRepository.findByName(roleType)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleType));
            newRoles.add(role);
        });
        user.setRoles(newRoles);
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public void toggleUserEnabled(Long userId, boolean enabled) {
        User user = findById(userId);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .designation(user.getDesignation())
                .profileImageUrl(user.getProfileImageUrl())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
