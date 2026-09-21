package org.example.demo1.service;



import org.example.demo1.pojo.Login;
import org.springframework.stereotype.Service;

@Service
public interface LoginService {
    public Login selectByStuID(String stuID);

    boolean checkPassword(String password, String encodePassword);
}
