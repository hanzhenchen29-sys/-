package org.example.demo1.common;

public final class ErrorCode {
    public static final int SUCCESS = 0;
    public static final int BAD_REQUEST = 40001;
    public static final int UNAUTHORIZED = 40100;
    public static final int CAPTCHA_ERROR = 40101;
    public static final int LOGIN_FAILED = 40102;
    public static final int FORBIDDEN = 40300;
    public static final int NOT_FOUND = 40400;
    public static final int CONFLICT = 40900;
    public static final int SYSTEM_ERROR = 50000;

    private ErrorCode() {
    }
}
