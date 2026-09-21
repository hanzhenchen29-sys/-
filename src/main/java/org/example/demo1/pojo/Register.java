package org.example.demo1.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Register {
    private String username;//20251714201
    private String nickname;//kklll
    private String password;//123456
    private String captchaId;//uuid
    private String captchaCode;//图像验证码

}
