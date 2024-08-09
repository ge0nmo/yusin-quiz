package com.cpa.yusin.quiz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebMvcConfig implements WebMvcConfigurer
{
    private final long MAX_AGE_SECS = 3600;

}
