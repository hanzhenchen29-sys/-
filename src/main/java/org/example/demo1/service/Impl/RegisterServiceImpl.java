package org.example.demo1.service.Impl;

import org.example.demo1.mapper.LoginMapper;
import org.example.demo1.mapper.RegisterMapper;
import org.example.demo1.pojo.Login;
import org.example.demo1.pojo.Register;
import org.example.demo1.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RegisterServiceImpl implements RegisterService {
    private RegisterMapper registerMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private static final String KEY_PREFIX = "login_";

    @Override
    public Register selectByStuID(String username) {
        String key=KEY_PREFIX+username;
        String value=stringRedisTemplate.opsForValue().get(key);
        if (value != null) {
          Register user_info=new Register();
            user_info.setUsername(username);
            return user_info;
        }
        Register user_info= registerMapper.selectByStuID_SQL(username);
        if (user_info != null) {
            stringRedisTemplate.opsForValue().set(key, user_info.getPassword(), 30, TimeUnit.MINUTES);
        }else{
            stringRedisTemplate.opsForValue().set(key, "", 1, TimeUnit.MINUTES);
        }
        return user_info;
    }

    @Override
    public Register userRegister(Register register) {
        //密码加密
        String password = register.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        register.setPassword(encodedPassword);
        registerMapper.register(register);
        return register;
    }
}
