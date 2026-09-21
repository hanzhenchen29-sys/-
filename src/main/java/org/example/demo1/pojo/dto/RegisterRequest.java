package org.example.demo1.pojo.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String nickname;
    private String captchaId;
    private String captchaCode;
}
