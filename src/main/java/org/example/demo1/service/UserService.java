package org.example.demo1.service;

import java.util.Map;

public interface UserService {
    Map<String, Object> getCurrentUser();

    Map<String, Object> updateNickname(String nickname);
}
