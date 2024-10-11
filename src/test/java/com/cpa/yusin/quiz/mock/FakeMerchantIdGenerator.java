package com.cpa.yusin.quiz.mock;

import com.cpa.yusin.quiz.common.service.MerchantIdGenerator;

public class FakeMerchantIdGenerator implements MerchantIdGenerator
{
    @Override
    public String generateId(long memberId)
    {
        return memberId + "-" + "merchantId";
    }
}
