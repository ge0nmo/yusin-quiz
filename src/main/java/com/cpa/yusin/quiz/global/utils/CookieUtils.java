package com.cpa.yusin.quiz.global.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@Slf4j
public class CookieUtils
{
    private static final Gson gson = new Gson();

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name)
    {
        Cookie[] cookies = request.getCookies();

        if(cookies != null && cookies.length > 0) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }

        return Optional.empty();
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge)
    {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name)
    {
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length > 0) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }
    }

    public static String serialize(Object object)
    {
/*        log.info("object: {}", object);
        try {
            String jsonString = objectMapper.writeValueAsString(object);
            return Base64.getEncoder().encodeToString(jsonString.getBytes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }*/

        String json = gson.toJson(object);
        return Base64.getEncoder().encodeToString(json.getBytes());
    }

    public static <T> T deserialize(Cookie cookie, Class<T> cls)
    {
        if(cookie == null || cookie.getValue() == null){
            return null;
        }
        log.info("cookie = {}", cookie.getValue());
        byte[] decodedBytes = Base64.getDecoder().decode(cookie.getValue());
        String json = new String(decodedBytes);
        return gson.fromJson(json, cls);

/*        try{

            String jsonString = new String(decodedBytes);
            log.info("jsonString = {}", jsonString);
            T result = objectMapper.readValue(jsonString, cls);
            log.info("result = {}", result);
            return result;
        } catch (IOException e){
            throw new RuntimeException(e);
        }*/
    }
}
