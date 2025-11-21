package com.apiEtudiant.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.apiEtudiant.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final boolean securityEnabled;

    public AuthInterceptor(AuthService authService, @Value("${app.security.enabled:true}") boolean securityEnabled) {
        this.authService = authService;
        this.securityEnabled = securityEnabled;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!securityEnabled) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader(authService.getTokenHeaderName());
        authService.validateToken(token);
        return true;
    }
}
