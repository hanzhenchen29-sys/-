package org.example.demo1.controller;

import org.example.demo1.pojo.Result;
import org.example.demo1.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/users/me")
    public Result me() {
        return Result.success(userService.getCurrentUser());
    }

    @PutMapping("/api/users/me")
    public Result updateMe(@RequestBody Map<String, String> body) {
        return Result.success(userService.updateNickname(body.get("nickname")));
    }
}
