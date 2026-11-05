package com.cpa.yusin.quiz.report.infrastructure;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** 신고자가 고른 보기 번호 목록을 "1,3" 같은 콤마 문자열로 저장한다. 고르지 않았으면 빈 문자열이다. */
@Converter
public class ChoiceNumbersConverter implements AttributeConverter<List<Integer>, String> {
    @Override
    public String convertToDatabaseColumn(List<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return "";
        }
        return numbers.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    @Override
    public List<Integer> convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(",")).map(String::trim).filter(part -> !part.isEmpty())
                .map(Integer::valueOf).toList();
    }
}
