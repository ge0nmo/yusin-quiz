package com.cpa.yusin.quiz.global.details;

import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.infrastructure.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class MemberDetails implements UserDetails, OAuth2User
{
    private final MemberDomain memberDomain;
    private final Map<String, Object> attributes;

    public MemberDetails(MemberDomain memberDomain, Map<String, Object> attributes)
    {
        this.memberDomain = memberDomain;
        this.attributes = attributes;
    }

    @Override
    public String getPassword()
    {
        return memberDomain.getPassword();
    }

    @Override
    public String getUsername()
    {
        return memberDomain.getEmail();
    }

    @Override
    public String getName()
    {
        return memberDomain.getEmail();
    }

    @Override
    public Map<String, Object> getAttributes()
    {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return Collections.singletonList(new SimpleGrantedAuthority(memberDomain.getRole().name()));
    }

}
