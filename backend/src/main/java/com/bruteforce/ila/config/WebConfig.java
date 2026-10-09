package com.bruteforce.ila.config;

import com.bruteforce.ila.security.StudentScopeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StudentScopeInterceptor studentScopeInterceptor;

    public WebConfig(StudentScopeInterceptor studentScopeInterceptor) {
        this.studentScopeInterceptor = studentScopeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(studentScopeInterceptor).addPathPatterns("/api/**");
    }
}
