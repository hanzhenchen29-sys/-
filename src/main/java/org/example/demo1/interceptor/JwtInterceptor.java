package org.example.demo1.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.demo1.common.ErrorCode;
import org.example.demo1.context.UserContext;
import org.example.demo1.pojo.Result;
import org.example.demo1.service.TokenBlacklistService;
import org.example.demo1.service.cache.UserCacheService;
import org.example.demo1.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    private final TokenBlacklistService tokenBlacklistService;
    private final UserCacheService userCacheService;
    private final ObjectMapper objectMapper;

    public JwtInterceptor(TokenBlacklistService tokenBlacklistService,
                          UserCacheService userCacheService,
                          ObjectMapper objectMapper) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.userCacheService = userCacheService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            writeUnauthorized(response);
            return false;
        }
        try {
            Claims claims = JwtUtil.parseToken(token);
            if (tokenBlacklistService.isBlacklisted(claims.getId())) {
                writeUnauthorized(response);
                return false;
            }
            Long userId = JwtUtil.getUserId(claims);
            String username = String.valueOf(claims.get("username"));
            UserCacheService.AuthInfo authInfo = userCacheService.getAuthInfo(userId);
            if (authInfo.roles().isEmpty()) {
                // 用户不存在或未绑定任何可用角色，视为无效令牌
                writeUnauthorized(response);
                return false;
            }
            UserContext.set(userId, username, authInfo.roles(), authInfo.permissions());
            return true;
        } catch (Exception ex) {
            writeUnauthorized(response);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7).trim();
        }
        return request.getHeader("token");
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error("未登录或令牌无效", ErrorCode.UNAUTHORIZED)));
    }
}
