package org.example.demo1.context;

import java.util.Collections;
import java.util.List;

public final class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> ROLES = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> PERMISSIONS = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId, String username, List<String> roles, List<String> permissions) {
        USER_ID.set(userId);
        USERNAME.set(username);
        ROLES.set(roles);
        PERMISSIONS.set(permissions);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static List<String> getRoles() {
        List<String> roles = ROLES.get();
        return roles == null ? Collections.emptyList() : roles;
    }

    public static List<String> getPermissions() {
        List<String> permissions = PERMISSIONS.get();
        return permissions == null ? Collections.emptyList() : permissions;
    }

    public static boolean hasPermission(String permission) {
        return getPermissions().contains(permission);
    }

    public static boolean isAdmin() {
        return getRoles().contains("ADMIN");
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        ROLES.remove();
        PERMISSIONS.remove();
    }
}
