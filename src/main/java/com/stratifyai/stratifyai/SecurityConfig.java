package com.stratifyai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    // CORS Security - Only allow specific origins
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/analyze")
                .allowedOrigins("*")
                .allowedMethods("POST")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    // Add security headers to every response
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SecurityHeadersInterceptor());
    }
}
