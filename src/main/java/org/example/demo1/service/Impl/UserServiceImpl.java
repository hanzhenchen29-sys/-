package org.example.demo1.service.Impl;

import org.example.demo1.common.ErrorCode;
import org.example.demo1.context.UserContext;
import org.example.demo1.exception.BusinessException;
import org.example.demo1.mapper.SysUserMapper;
import org.example.demo1.pojo.entity.SysUser;
import org.example.demo1.service.UserService;
import org.example.demo1.service.cache.UserCacheService;
import org.example.demo1.util.DateTimeUtil;
import org.example.demo1.util.PermissionChecker;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private final SysUserMapper sysUserMapper;
    private final UserCacheService userCacheService;

    public UserServiceImpl(SysUserMapper sysUserMapper, UserCacheService userCacheService) {
        this.sysUserMapper = sysUserMapper;
        this.userCacheService = userCacheService;
    }

    @Override
    public Map<String, Object> getCurrentUser() {
        PermissionChecker.require("user:profile");
        return buildProfile(UserContext.getUserId());
    }

    @Override
    public Map<String, Object> updateNickname(String nickname) {
        PermissionChecker.require("user:profile");
        if (!StringUtils.hasText(nickname)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "参数错误");
        }
        Long userId = UserContext.getUserId();
        sysUserMapper.updateNickname(userId, nickname);
        SysUser user = sysUserMapper.findById(userId);
        if (user != null) {
            userCacheService.evictUsername(user.getUsername());
        }
        userCacheService.evictAuth(userId);
        return buildProfile(userId);
    }

    private Map<String, Object> buildProfile(Long userId) {
        SysUser user = sysUserMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在");
        }
        UserCacheService.AuthInfo authInfo = userCacheService.getAuthInfo(userId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("phone", user.getPhone());
        data.put("role", authInfo.primaryRole());
        data.put("roles", authInfo.roles());
        data.put("permissions", authInfo.permissions());
        data.put("createTime", DateTimeUtil.format(user.getCreateTime()));
        return data;
    }
}
