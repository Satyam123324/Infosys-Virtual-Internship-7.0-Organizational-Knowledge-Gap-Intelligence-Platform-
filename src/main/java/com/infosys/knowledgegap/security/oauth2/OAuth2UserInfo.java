package com.infosys.knowledgegap.security.oauth2;

import java.util.Map;

/**
 * Abstracts over the different attribute shapes returned by different OAuth2 providers
 * (Google vs GitHub have completely different JSON fields for the same concepts).
 */
public abstract class OAuth2UserInfo {

    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();
}
