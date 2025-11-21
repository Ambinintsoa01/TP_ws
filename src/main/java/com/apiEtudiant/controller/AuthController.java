package com.apiEtudiant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apiEtudiant.dto.ApiResponse;
import com.apiEtudiant.dto.LoginRequest;
import com.apiEtudiant.dto.LoginResponse;
import com.apiEtudiant.service.AuthService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        var token = authService.authenticate(request.getUsername(), request.getPassword());
        LoginResponse data = new LoginResponse(token.token(), token.expiresAt());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
