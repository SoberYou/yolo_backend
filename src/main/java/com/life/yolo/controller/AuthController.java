package com.life.yolo.controller;

import com.life.yolo.entity.User;
import com.life.yolo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication Management")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "WeChat Login", description = "Login with WeChat JS Code")
    public User login(@RequestBody LoginRequest request) {
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new IllegalArgumentException("Code is required");
        }
        return authService.login(request.getCode());
    }

    @Data
    public static class LoginRequest {
        private String code;
    }
}
