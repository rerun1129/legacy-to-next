package com.freightos.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class EndpointLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String qs = request.getQueryString();
        String uri = qs == null ? request.getRequestURI() : request.getRequestURI() + "?" + qs;
        log.info(">>> {} {}", request.getMethod(), uri);
        return true;
    }
}
