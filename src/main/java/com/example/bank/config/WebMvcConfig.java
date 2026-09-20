package com.example.bank.config;

import com.example.bank.interceptor.RequestLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor loggingInterceptor;

    public WebMvcConfig(RequestLoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**") // применять только к /*
                .excludePathPatterns("/auth/**"); // кроме auth-эндпоинтов
    }
}