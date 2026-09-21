package org.example.demo1.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.example.demo1.common.ErrorCode;
import org.example.demo1.exception.BusinessException;
import org.example.demo1.mapper.RbacMapper;
import org.example.demo1.mapper.SysUserMapper;
import org.example.demo1.pojo.dto.LoginRequest;
import org.example.demo1.pojo.dto.LoginResponse;
import org.example.demo1.pojo.dto.RegisterRequest;
import org.example.demo1.pojo.entity.SysUser;
import org.example.demo1.service.AuthService;
import org.example.demo1.service.CaptchaService;
import org.example.demo1.service.TokenBlacklistService;
import org.example.demo1.service.cache.UserCacheService;
import org.example.demo1.util.JwtUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private final CaptchaService captchaService;
    private final SysUserMapper sysUserMapper;
    private final RbacMapper rbacMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserCacheService userCacheService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthServiceImpl(CaptchaService captchaService,
                           SysUserMapper sysUserMapper,
                           RbacMapper rbacMapper,
                           PasswordEncoder passwordEncoder,
                           UserCacheService userCacheService,
                           TokenBlacklistService tokenBlacklistService) {
        this.captchaService = captchaService;
        this.sysUserMapper = sysUserMapper;
        this.rbacMapper = rbacMapper;
        this.passwordEncoder = passwordEncoder;
        this.userCacheService = userCacheService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public Map<String, Object> createCaptcha() {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String imageBase64 = captchaService.createCaptcha(captchaId);
        Map<String, Object> data = new HashMap<>();
        data.put("captchaId", captchaId);
        data.put("imageBase64", imageBase64);
        return data;
    }

    @Override
    @Transactional
    public Long register(RegisterRequest request) {
        validateRegisterRequest(request);
        verifyCaptcha(request.getCaptchaId(), request.getCaptchaCode());

        SysUser existing = userCacheService.findByUsername(request.getUsername());
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        try {
            sysUserMapper.insert(user);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }

        Long userRoleId = rbacMapper.findRoleIdByCode("USER");
        if (userRoleId != null) {
            rbacMapper.bindUserRole(user.getId(), userRoleId);
        }

        userCacheService.evictUsername(request.getUsername());
        userCacheService.evictAuth(user.getId());
        return user.getId();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数错误");
        }
        verifyCaptcha(request.getCaptchaId(), request.getCaptchaCode());

        SysUser user = userCacheService.findByUsername(request.getUsername());
        if (user == null) {
            userCacheService.recordLoginFailure(request.getUsername());
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "用户名或密码错误");
        }

        UserCacheService.AuthInfo authInfo = userCacheService.getAuthInfo(user.getId());
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(JwtUtil.EXPIRE_SECONDS)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(authInfo.primaryRole())
                .roles(authInfo.roles())
                .permissions(authInfo.permissions())
                .build();
    }

    @Override
    public void logout(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        try {
            var claims = JwtUtil.parseToken(token);
            tokenBlacklistService.blacklist(claims.getId(), JwtUtil.remainingSeconds(claims));
        } catch (Exception ex) {
            log.warn("logout ignored invalid token");
        }
    }

    private void verifyCaptcha(String captchaId, String captchaCode) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数错误");
        }
        if (!captchaService.verifyCaptcha(captchaId, captchaCode)) {
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR, "验证码错误或过期");
        }
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || request.getUsername().length() < 4 || request.getUsername().length() > 32) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数错误");
        }
        if (!StringUtils.hasText(request.getPassword()) || request.getPassword().length() < 6 || request.getPassword().length() > 32) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数错误");
        }
        if (!StringUtils.hasText(request.getNickname()) || request.getNickname().length() < 1 || request.getNickname().length() > 32) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数错误");
        }
    }
}
