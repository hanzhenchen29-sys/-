package org.example.demo1.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RbacMapper {

    @Select("""
            SELECT r.role_code
            FROM sys_role r
            INNER JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId} AND r.status = 1
            """)
    List<String> findRoleCodesByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT p.perm_code
            FROM sys_permission p
            INNER JOIN sys_role_permission rp ON rp.permission_id = p.id
            INNER JOIN sys_user_role ur ON ur.role_id = rp.role_id
            WHERE ur.user_id = #{userId} AND p.status = 1
            """)
    List<String> findPermissionsByUserId(@Param("userId") Long userId);

    @Select("SELECT id FROM sys_role WHERE role_code = #{roleCode} LIMIT 1")
    Long findRoleIdByCode(@Param("roleCode") String roleCode);

    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int bindUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
