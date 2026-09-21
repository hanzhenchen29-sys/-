package org.example.demo1.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String tokenType;
    private long expiresIn;
    private Long userId;
    private String username;
    private String nickname;
    private String role;
    private List<String> roles;
    private List<String> permissions;
}
