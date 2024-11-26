package com.cpa.yusin.quiz.global.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DateUtils
{
    public List<Integer> getExamYearList()
    {
        List<Integer> list = new ArrayList<>();
        int year = LocalDate.now().getYear();

        for(int i = 2013; i <= year; i++){
            list.add(i);
        }

        return list;
    }
}
