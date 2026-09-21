package org.example.demo1.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.demo1.pojo.Login;

@Mapper
public interface LoginMapper {

    @Select("select * from login where username = #{username}")
    Login selectByStuID_SQL(String username);
}