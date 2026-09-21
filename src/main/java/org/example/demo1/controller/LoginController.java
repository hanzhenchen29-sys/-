package org.example.demo1.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.demo1.pojo.Login;
import org.example.demo1.pojo.Result;
import org.example.demo1.service.CaptchaService;
import org.example.demo1.service.LoginService;
import org.example.demo1.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
public class LoginController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private CaptchaService captchaService;
    /*
    * 现根据username（学号）查Redis和MySQL
    * 有了接着登录，没有返回用户不存在
    * */


    @PostMapping("/api/auth/login")
    public Result login(@RequestBody Login login, HttpServletResponse response){
        String stuID = login.getUsername();
        String password = login.getPassword();
        String captchaId = login.getCaptchaId();
        String captchaCode = login.getCaptchaCode();

        // 根据学号查询用户
        Login user_info = loginService.selectByStuID(stuID);
        if(user_info== null){
            return Result.error("用户不存在",40400);
        }
        boolean passwordOk = loginService.checkPassword(password, login.getPassword());
        if (!passwordOk) {
            return Result.error("密码错误",40102);
        }
        if(!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)){
            return Result.error("验证码不能为空",40002);
        }
        boolean captchaPass = captchaService.verifyCaptcha(captchaId, captchaCode);
        if(!captchaPass){
            return Result.error("验证码错误或已过期",40101);
        }

        //生成JWT令牌
        Map<String,Object> info=new HashMap<>();
        info.put("stuID",stuID);
        String token = JWTUtil.generateJwt(info);
        response.setHeader("token",token);
        log.info("学号:{} 登录成功", stuID);
        return Result.success();
    }

    @GetMapping("/api/auth/captcha")
    public Result getCaptcha(HttpServletResponse response){
        // 禁用GET缓存
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        String captchaId = UUID.randomUUID().toString().replace("-","");
        log.info("captchaId：{}", captchaId);
        String base64Img = captchaService.createCaptcha(captchaId);
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("captchaId",captchaId);
        dataMap.put("imageBase64",base64Img);
        return Result.success(dataMap);
    }
}