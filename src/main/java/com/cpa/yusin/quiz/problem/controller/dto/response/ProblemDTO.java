package com.cpa.yusin.quiz.problem.controller.dto.response;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
public record ProblemDTO(long id, String content, int number, String explanation, List<ChoiceResponse> choices) {

}
