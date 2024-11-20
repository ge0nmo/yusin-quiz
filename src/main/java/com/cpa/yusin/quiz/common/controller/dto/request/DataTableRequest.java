package com.cpa.yusin.quiz.common.controller.dto.request;

import lombok.Data;

@Data
public class DataTableRequest
{
    private int page;
    private int size;

    public DataTableRequest()
    {
        this.page = 0;
        this.size = 10;
    }
}
