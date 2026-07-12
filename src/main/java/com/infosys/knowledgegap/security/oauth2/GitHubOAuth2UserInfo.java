package com.infosys.knowledgegap.security.oauth2;

import java.util.Map;

/**
 * GitHub's user API returns a different shape than Google: "id" is numeric, "login" is the
 * username, "name" can be null (many users don't set a display name), and "email" can be null
 * if the user has kept their email private — GitHub only returns it if the account's primary
 * email is public, even with the user:email scope granted for many privacy-conscious users.
 * In that case we fall back to a deterministic placeholder so the account can still be created;
 * the user can update their real email later from their profile if needed.
 */
public class GitHubOAuth2UserInfo extends OAuth2UserInfo {

    public GitHubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        Object id = attributes.get("id");
        return id != null ? id.toString() : null;
    }

    @Override
    public String getName() {
        Object name = attributes.get("name");
        if (name != null && !name.toString().isBlank()) {
            return name.toString();
        }
        Object login = attributes.get("login");
        return login != null ? login.toString() : "GitHub User";
    }

    @Override
    public String getEmail() {
        Object email = attributes.get("email");
        if (email != null && !email.toString().isBlank()) {
            return email.toString();
        }
        Object login = attributes.get("login");
        String username = login != null ? login.toString() : getId();
        return username + "@github.local";
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
}
