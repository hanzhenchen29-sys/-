package org.example.demo1.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.demo1.pojo.entity.SysUser;

@Mapper
public interface SysUserMapper {

    @Select("SELECT id, username, password, nickname, phone, status, create_time, update_time FROM sys_user WHERE username = #{username}")
    SysUser findByUsername(@Param("username") String username);

    @Select("SELECT id, username, password, nickname, phone, status, create_time, update_time FROM sys_user WHERE id = #{id}")
    SysUser findById(@Param("id") Long id);

    @Insert("INSERT INTO sys_user (username, password, nickname, phone, status) VALUES (#{username}, #{password}, #{nickname}, #{phone}, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser user);

    @Update("UPDATE sys_user SET nickname = #{nickname}, update_time = NOW() WHERE id = #{id}")
    int updateNickname(@Param("id") Long id, @Param("nickname") String nickname);

    @Select("SELECT id FROM sys_user WHERE id = #{id} FOR UPDATE")
    Long lockByIdForUpdate(@Param("id") Long id);
}
