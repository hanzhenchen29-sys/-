package org.example.demo1.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.demo1.pojo.Result;
import org.example.demo1.pojo.dto.LoginRequest;
import org.example.demo1.pojo.dto.RegisterRequest;
import org.example.demo1.service.AuthService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    //获取验证码
    @GetMapping("/api/auth/captcha")
    public Result captcha(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        return Result.success(authService.createCaptcha());
    }
    //注册
    @PostMapping("/api/auth/register")
    public Result register(@RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        return Result.success(data);
    }
    //登录
    @PostMapping("/api/auth/login")
    public Result login(@RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/api/auth/logout")
    public Result logout(@RequestHeader(value = "Authorization", required = false) String authorization,
                         @RequestHeader(value = "token", required = false) String tokenHeader) {
        String token = resolveToken(authorization, tokenHeader);
        authService.logout(token);
        return Result.success();
    }

    private String resolveToken(String authorization, String tokenHeader) {
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7).trim();
        }
        return tokenHeader;
    }
}
