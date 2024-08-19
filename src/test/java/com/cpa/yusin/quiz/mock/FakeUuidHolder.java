package com.cpa.yusin.quiz.mock;

import com.cpa.yusin.quiz.common.service.UuidHolder;

public class FakeUuidHolder implements UuidHolder
{
    private final String uuid;

    public FakeUuidHolder(String uuid)
    {
        this.uuid = uuid;
    }

    @Override
    public String getRandom()
    {
        return uuid;
    }
}
