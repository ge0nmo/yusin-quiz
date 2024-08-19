package com.cpa.yusin.quiz.mock;

import com.cpa.yusin.quiz.global.security.oauth2.user.OAuth2UserInfo;
import com.cpa.yusin.quiz.member.domain.type.Platform;

import java.util.Map;

public class FakeOAuth2UserInfo extends OAuth2UserInfo
{
    private final String id;
    private final String name;
    private final String email;
    private final Platform platform;

    public FakeOAuth2UserInfo(Map<String, Object> attributes, String id, String name, String email, Platform platform)
    {
        super(attributes);
        this.id = id;
        this.name = name;
        this.email = email;
        this.platform = platform;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String getEmail()
    {
        return this.email;
    }

    @Override
    public Platform getPlatform()
    {
        return this.platform;
    }
}
