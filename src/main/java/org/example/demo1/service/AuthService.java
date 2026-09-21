package org.example.demo1.service;

import org.example.demo1.pojo.dto.LoginRequest;
import org.example.demo1.pojo.dto.LoginResponse;
import org.example.demo1.pojo.dto.RegisterRequest;

import java.util.Map;

public interface AuthService {
    Map<String, Object> createCaptcha();

    Long register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String token);
}
