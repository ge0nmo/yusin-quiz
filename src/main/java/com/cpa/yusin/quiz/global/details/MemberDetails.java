package com.cpa.yusin.quiz.global.details;

import com.cpa.yusin.quiz.member.domain.Member;
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
    private final Member member;
    private final Map<String, Object> attributes;

    public MemberDetails(Member member, Map<String, Object> attributes)
    {
        this.member = member;
        this.attributes = attributes;
    }

    @Override
    public String getPassword()
    {
        return member.getPassword();
    }

    @Override
    public String getUsername()
    {
        return member.getEmail();
    }

    @Override
    public String getName()
    {
        return member.getEmail();
    }

    @Override
    public Map<String, Object> getAttributes()
    {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return Collections.singletonList(new SimpleGrantedAuthority(member.getRole().name()));
    }

}
