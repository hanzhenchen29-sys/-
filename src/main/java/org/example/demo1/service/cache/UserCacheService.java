package org.example.demo1.service.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.demo1.pojo.entity.SysUser;
import org.example.demo1.mapper.RbacMapper;
import org.example.demo1.mapper.SysUserMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserCacheService {
    private static final String USER_KEY_PREFIX = "user:username:";
    private static final String AUTH_KEY_PREFIX = "user:auth:";
    private static final String NULL_SENTINEL = "__NULL__";
    private static final long USER_TTL_MINUTES = 30;

    private final StringRedisTemplate redisTemplate;
    private final SysUserMapper sysUserMapper;
    private final RbacMapper rbacMapper;
    private final ObjectMapper objectMapper;

    public UserCacheService(StringRedisTemplate redisTemplate,
                            SysUserMapper sysUserMapper,
                            RbacMapper rbacMapper,
                            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.sysUserMapper = sysUserMapper;
        this.rbacMapper = rbacMapper;
        this.objectMapper = objectMapper;
    }

    public SysUser findByUsername(String username) {
        String key = USER_KEY_PREFIX + username;
        String cached = redisTemplate.opsForValue().get(key);
        if (NULL_SENTINEL.equals(cached)) {
            return null;
        }
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, SysUser.class);
            } catch (JsonProcessingException e) {
                redisTemplate.delete(key);
            }
        }
        SysUser user = sysUserMapper.findByUsername(username);
        cacheUser(username, user);
        return user;
    }

    public SysUser findById(Long userId) {
        return sysUserMapper.findById(userId);
    }

    public AuthInfo getAuthInfo(Long userId) {
        String key = AUTH_KEY_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, AuthInfo.class);
            } catch (JsonProcessingException e) {
                redisTemplate.delete(key);
            }
        }
        List<String> roles = rbacMapper.findRoleCodesByUserId(userId);
        List<String> permissions = rbacMapper.findPermissionsByUserId(userId);
        AuthInfo authInfo = new AuthInfo(roles, permissions);
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(authInfo), USER_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException ignored) {
            // 缓存失败不影响主流程
        }
        return authInfo;
    }

    public void evictUsername(String username) {
        redisTemplate.delete(USER_KEY_PREFIX + username);
    }

    /**
     * 记录登录失败：向 Redis 写入空值，key = username + "login"
     */
    public void recordLoginFailure(String username) {
        redisTemplate.opsForValue().set(username + "login", "");
    }

    public void evictAuth(Long userId) {
        redisTemplate.delete(AUTH_KEY_PREFIX + userId);
    }

    private void cacheUser(String username, SysUser user) {
        String key = USER_KEY_PREFIX + username;
        try {
            if (user == null) {
                redisTemplate.opsForValue().set(key, NULL_SENTINEL, 1, TimeUnit.MINUTES);
            } else {
                redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(user), USER_TTL_MINUTES, TimeUnit.MINUTES);
            }
        } catch (JsonProcessingException ignored) {
            // 缓存失败不影响主流程
        }
    }

    public record AuthInfo(List<String> roles, List<String> permissions) {
        public String primaryRole() {
            if (roles.contains("ADMIN")) {
                return "ADMIN";
            }
            return roles.isEmpty() ? "USER" : roles.get(0);
        }
    }
}
