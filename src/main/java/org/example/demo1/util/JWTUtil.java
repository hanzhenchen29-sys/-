package org.example.demo1.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具类（HS256）。
 * 密钥默认使用内置常量，可通过环境变量 JWT_SECRET 覆盖（生产环境务必配置）。
 */
public final class JwtUtil {
    public static final long EXPIRE_SECONDS = 86400L;

    private static final String DEFAULT_SECRET =
            "QxRepair-Assessment-JwtSecret-Key-2026-Demo1-For-School-Project";

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(resolveSecret().getBytes(StandardCharsets.UTF_8));

    private JwtUtil() {
    }

    private static String resolveSecret() {
        String env = System.getenv("JWT_SECRET");
        return (env == null || env.isBlank()) ? DEFAULT_SECRET : env;
    }

    public static String generateToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString().replace("-", ""))
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + EXPIRE_SECONDS * 1000))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public static long remainingSeconds(Claims claims) {
        Date exp = claims.getExpiration();
        if (exp == null) {
            return EXPIRE_SECONDS;
        }
        long remain = (exp.getTime() - System.currentTimeMillis()) / 1000;
        return Math.max(remain, 1);
    }
}
