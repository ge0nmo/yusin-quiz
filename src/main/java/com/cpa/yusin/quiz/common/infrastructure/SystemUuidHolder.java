package com.cpa.yusin.quiz.common.infrastructure;

import com.cpa.yusin.quiz.common.service.UuidHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Qualifier("systemUuidHolder")
@Component
public class SystemUuidHolder implements UuidHolder
{
    @Override
    public String getRandom()
    {
        return UUID.randomUUID().toString();
    }
}
