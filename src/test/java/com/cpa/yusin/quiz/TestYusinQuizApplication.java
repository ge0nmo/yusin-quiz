package com.cpa.yusin.quiz;

import org.springframework.boot.SpringApplication;

public class TestYusinQuizApplication
{

    public static void main(String[] args)
    {
        SpringApplication.from(YusinQuizApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
