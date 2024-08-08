package com.cpa.yusin.quiz.global.details;

import com.cpa.yusin.quiz.domain.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class MemberDetails implements UserDetails
{
    private final Member member;

    public MemberDetails(Member member)
    {
        this.member = member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return Collections.singletonList(new SimpleGrantedAuthority(member.getRole().name()));
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
}
