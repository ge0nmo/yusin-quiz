package com.cpa.yusin.quiz.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalResponse<T> {
    private T data;
    private PageInfo pageInfo;

    public GlobalResponse() {
    }

    public GlobalResponse(T data) {
        this.data = data;
    }
}


