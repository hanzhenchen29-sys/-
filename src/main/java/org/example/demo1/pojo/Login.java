package org.example.demo1.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Login {
    private String password;
    private String username;
    private String captchaId;
    private String captchaCode;
}
