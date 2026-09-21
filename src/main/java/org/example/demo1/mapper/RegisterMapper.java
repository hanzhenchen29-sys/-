package org.example.demo1.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.demo1.pojo.Register;

@Mapper
public interface RegisterMapper {
    @Select("select * from login where username = #{username}")
    Register selectByStuID_SQL(String username);

    @Insert("INSERT into user_info (username,nickname,password) values (#{username},#{nickname},#{password})")
    void register(Register register);
}
