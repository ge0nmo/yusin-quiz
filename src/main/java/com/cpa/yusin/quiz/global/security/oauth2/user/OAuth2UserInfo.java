package com.cpa.yusin.quiz.global.security.oauth2.user;

import com.cpa.yusin.quiz.member.domain.type.Platform;

import java.util.Map;

public abstract class OAuth2UserInfo
{
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes)
    {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes()
    {
        return attributes;
    }

    public abstract String getId();

    public abstract String getName();

    public abstract String getEmail();

    public abstract Platform getPlatform();
}
