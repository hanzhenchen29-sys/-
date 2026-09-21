package org.example.demo1.service;

import org.example.demo1.pojo.Register;

public interface RegisterService {
    Register selectByStuID(String username);

    Register userRegister(Register register);
}
