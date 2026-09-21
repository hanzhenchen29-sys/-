package org.example.demo1.service.Impl;

import org.example.demo1.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    @Autowired
    @Qualifier("captchaRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long EXPIRE_SECONDS = 120;


    @Override
    public String createCaptcha(String captchaId) {
        // 生成验证码图片
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120,40,4,40);
        String code = lineCaptcha.getCode();
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + captchaId, code, EXPIRE_SECONDS, TimeUnit.SECONDS);
        // 转Base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        lineCaptcha.write(outputStream);
        String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        return "data:image/png;base64," + base64;
    }

    @Override
    public boolean verifyCaptcha(String captchaId, String imgCode) {
        String key = CAPTCHA_PREFIX + captchaId;
        String realCode = redisTemplate.opsForValue().get(key);
        if(realCode == null){
            return false;
        }
        // 校验后直接删除，防止重复使用
        redisTemplate.delete(key);
        // 忽略大小写匹配
        return realCode.equalsIgnoreCase(imgCode);
    }
}