package org.example.demo1.service;

import jakarta.servlet.http.HttpServletResponse;

public interface CaptchaService {
    boolean verifyCaptcha(String captchaId, String imgCode);

    String createCaptcha(String captchaId);
}
