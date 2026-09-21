package org.example.demo1.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;
//请求头的工具类
public class JWTUtil {
    private static Long EXPIRE = 1000L*60*60*4;
    private static String SIGN_KEY ="user";


    public static String generateJwt(Map<String,Object> claims){
        String token = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256,"user")//设置签名算法
                .setClaims(claims)//设置给定的值
                .setExpiration(new Date(System.currentTimeMillis()+EXPIRE))//令牌的有效期
                .compact();//生成令牌
        return token;
    }

    public static Claims parseJWT(String jwt){
        Claims claims = Jwts.parser().setSigningKey(SIGN_KEY )//设置解析的密钥
                .parseClaimsJws(jwt)
                .getBody();
        return claims;
    }
}
