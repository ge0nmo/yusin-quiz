package com.cpa.yusin.quiz.global.utils;

public class ApplicationConstants
{
    public static final String[] API_ENDPOINT_WHITELIST = {
            "/css/**",
            "/img/**",
            "/js/**",
            "/dist/**",
            "/less/**",
            "/pages/**",
            "/scss/**",
            "/vendor/**",

            "/api/v1/home/login",
            "/api/v1/oauth2/**",
            "/api/v1/sign-up",
            "/api/v1/file/**",
            "/api/v1/payment/**",
            "/hc",
            "/env",
            "/api/v1/login",


    };

    public static final String[] FORM_ENDPOINT_WHITELIST = {
            "/css/**",
            "/img/**",
            "/js/**",
            "/dist/**",
            "/less/**",
            "/pages/**",
            "/scss/**",
            "/vendor/**",


            "/admin/login",

    };
}
