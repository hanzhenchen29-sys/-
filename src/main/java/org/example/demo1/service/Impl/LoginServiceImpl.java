package org.example.demo1.service.Impl;


import org.example.demo1.mapper.LoginMapper;
import org.example.demo1.pojo.Login;
import org.example.demo1.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private LoginMapper loginMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;


    private static final String KEY_PREFIX = "login_";
    @Override
    public Login selectByStuID(String username) {
        String key=KEY_PREFIX+username;
        //根据key查Redis，看看用户存不存在
        String value=stringRedisTemplate.opsForValue().get(key);
        //用户存在
        if (value != null) {
            Login login=new Login();
            login.setUsername(username);
            return login;
        }
        //不存在接着去MySQL里查
        Login login= loginMapper.selectByStuID_SQL(username);
        if (login != null) {
            stringRedisTemplate.opsForValue().set(key, login.getPassword(), 3000, TimeUnit.MINUTES);
        }else{
            //数据库也没有，Redis存空字符串
            stringRedisTemplate.opsForValue().set(key, "", 1, TimeUnit.MINUTES);
        }
        return login;
    }

    @Override
    public boolean checkPassword(String password, String encodePassword) {
        //matches() 内部会拿明文，使用密文中自带的盐值，重新做一次加密运算，对比两次加密结果是否一致。
        return passwordEncoder.matches(password, encodePassword);
    }
}
