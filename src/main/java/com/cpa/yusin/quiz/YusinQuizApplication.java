package com.cpa.yusin.quiz;

import com.cpa.yusin.quiz.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableConfigurationProperties(AppProperties.class)
@EnableJpaAuditing
@SpringBootApplication
public class YusinQuizApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(YusinQuizApplication.class, args);
    }

}
