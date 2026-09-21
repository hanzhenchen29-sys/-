package org.example.demo1.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.demo1.pojo.Login;
import org.example.demo1.pojo.Register;
import org.example.demo1.pojo.Result;
import org.example.demo1.service.CaptchaService;
import org.example.demo1.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class RegisterController {

    @Resource
    private RegisterService registerService;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private CaptchaService captchaService;

    @PostMapping("/api/auth/register")
    public Result register(@RequestBody Register register){
        String username = register.getUsername();
        String password = register.getPassword();
        String nickname = register.getNickname();
        String captchaId = register.getCaptchaId();
        String captchaCode = register.getCaptchaCode();
        if(!StringUtils.hasText(username) || !StringUtils.hasText(password)){
            return Result.error("用户名和密码不能为空",40001);
        }
        if(!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)){
            return Result.error("验证码不能为空",40002);
        }
        boolean captchaPass = captchaService.verifyCaptcha(captchaId, captchaCode);
        if(!captchaPass){
            return Result.error("验证码错误或已过期",40101);
        }
        /*先拿着username去数据库里看看有没有*/
        Register user_info = registerService.selectByStuID(username);
        if(user_info!= null){
            return Result.error("用户已经存在，请直接登录",40300);
        }
        Register registerResult = registerService.userRegister(register);
        log.info("注册成功");
        return Result.success(registerResult);
    }
}
