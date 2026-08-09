package com.cpa.yusin.quiz.mock;

import com.cpa.yusin.quiz.common.service.UuidHolder;

public class FakeUuidHolder implements UuidHolder {
    private final String value;

    public FakeUuidHolder(String value) {
        this.value = value;
    }

    @Override
    public String getRandom() {
        return value;
    }
}
