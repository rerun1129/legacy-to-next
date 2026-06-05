package com.freightos.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final EndpointLoggingInterceptor endpointLoggingInterceptor;

    public WebMvcConfig(EndpointLoggingInterceptor endpointLoggingInterceptor) {
        this.endpointLoggingInterceptor = endpointLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(endpointLoggingInterceptor);
    }
}
