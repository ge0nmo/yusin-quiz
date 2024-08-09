package com.cpa.yusin.quiz.security.oauth2.user;

import com.cpa.yusin.quiz.domain.entity.type.Platform;

import java.util.Map;

public class OAuth2UserInfoFactory
{
    public static OAuth2UserInfo getOAuthUserInfo(String registrationId, Map<String, Object> attributes)
    {
        if(registrationId.equalsIgnoreCase(Platform.GOOGLE.name())){
            return new GoogleOAuth2UserInfo(attributes);
        } else{
            throw new RuntimeException("sorry. log in with " + registrationId + "does not work");
        }
    }
}
