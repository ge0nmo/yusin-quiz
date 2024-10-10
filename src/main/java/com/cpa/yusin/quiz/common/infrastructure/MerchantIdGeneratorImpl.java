package com.cpa.yusin.quiz.common.infrastructure;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.common.service.MerchantIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MerchantIdGeneratorImpl implements MerchantIdGenerator
{
    private final ClockHolder clockHolder;
    private static final String PID = "pid-";

    @Override
    public String generatePID(long memberId)
    {
        return PID + memberId + "-" + clockHolder.getCurrentTime();
    }
}
