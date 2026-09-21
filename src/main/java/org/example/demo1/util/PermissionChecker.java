package org.example.demo1.util;

import org.example.demo1.common.ErrorCode;
import org.example.demo1.context.UserContext;
import org.example.demo1.exception.BusinessException;

public final class PermissionChecker {
    private PermissionChecker() {
    }

    public static void require(String permission) {
        if (!UserContext.hasPermission(permission)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限");
        }
    }
}
