package com.example.bank.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    // Вызывается ДО контроллера
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = (auth != null && auth.isAuthenticated())
                ? auth.getName() : "anonymous";

        log.info("→ {} {} | user: {}",
                request.getMethod(), request.getRequestURI(), user);

        // Сохраняем время старта для расчёта длительности
        request.setAttribute("startTime", System.currentTimeMillis());

        return true; // true = продолжить цепочку, false = прервать
    }

    // Вызывается ПОСЛЕ контроллера (но до записи ответа)
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {
        // Для REST API обычно не нужен
    }

    // Вызывается ПОСЛЕ записи ответа (всегда, даже при исключении)
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        long startTime = (long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;

        log.info("← {} {} | status: {} | {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration);

        if (ex != null) {
            log.error("Исключение при обработке запроса", ex);
        }
    }
}