package com.infosys.knowledgegap.security.oauth2;

import com.infosys.knowledgegap.entity.Role;
import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.enums.AuthProvider;
import com.infosys.knowledgegap.enums.RoleType;
import com.infosys.knowledgegap.repository.RoleRepository;
import com.infosys.knowledgegap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        return processOAuth2User(registrationId, oAuth2User);
    }

    private OAuth2User processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new OAuth2AuthenticationException("Email not found from " + registrationId + " OAuth2 provider");
        }

        AuthProvider provider = "github".equalsIgnoreCase(registrationId) ? AuthProvider.GITHUB : AuthProvider.GOOGLE;

        User user = userRepository.findByEmail(userInfo.getEmail())
                .map(existing -> updateExistingUser(existing, userInfo))
                .orElseGet(() -> registerNewOAuth2User(userInfo, provider));

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private User registerNewOAuth2User(OAuth2UserInfo userInfo, AuthProvider provider) {
        Role employeeRole = roleRepository.findByName(RoleType.EMPLOYEE)
                .orElseThrow(() -> new IllegalStateException("Default EMPLOYEE role not seeded"));

        User user = User.builder()
                .fullName(userInfo.getName())
                .email(userInfo.getEmail())
                .provider(provider)
                .providerId(userInfo.getId())
                .profileImageUrl(userInfo.getImageUrl())
                .enabled(true)
                .roles(Set.of(employeeRole))
                .build();

        return userRepository.save(user);
    }

    private User updateExistingUser(User existing, OAuth2UserInfo userInfo) {
        existing.setFullName(userInfo.getName());
        existing.setProfileImageUrl(userInfo.getImageUrl());
        return userRepository.save(existing);
    }
}
