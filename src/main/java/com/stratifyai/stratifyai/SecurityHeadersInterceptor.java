package com.stratifyai;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SecurityHeadersInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) {

        // Prevent clickjacking attacks
        response.setHeader("X-Frame-Options", "DENY");

        // Prevent MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Enable XSS protection
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Force HTTPS
        response.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains");

        // Content Security Policy
        response.setHeader("Content-Security-Policy",
                "default-src 'self'");

        return true;
    }
}