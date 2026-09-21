package org.example.demo1.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.demo1.pojo.entity.RepairCategory;

import java.util.List;

@Mapper
public interface RepairCategoryMapper {

    @Select("SELECT id, name, status, sort_no, create_time, update_time FROM repair_category WHERE status = 1 ORDER BY sort_no ASC, id ASC")
    List<RepairCategory> findEnabled();

    @Select("SELECT id, name, status, sort_no, create_time, update_time FROM repair_category WHERE id = #{id}")
    RepairCategory findById(@Param("id") Long id);

    @Select("SELECT id, name, status, sort_no, create_time, update_time FROM repair_category WHERE id = #{id} FOR UPDATE")
    RepairCategory findByIdForUpdate(@Param("id") Long id);

    @Select("""
            <script>
            SELECT id, name, status, sort_no, create_time, update_time FROM repair_category
            <where>
              <if test='name != null and name != ""'>AND name LIKE CONCAT('%', #{name}, '%')</if>
              <if test='status != null'>AND status = #{status}</if>
            </where>
            ORDER BY sort_no ASC, id ASC
            </script>
            """)
    List<RepairCategory> findByCondition(@Param("name") String name, @Param("status") Integer status);

    @Insert("INSERT INTO repair_category (name, status, sort_no) VALUES (#{name}, 1, #{sortNo})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RepairCategory category);

    @Update("UPDATE repair_category SET name = #{name}, status = #{status}, sort_no = #{sortNo}, update_time = NOW() WHERE id = #{id}")
    int update(RepairCategory category);

    @Delete("DELETE FROM repair_category WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM repair_ticket WHERE category_id = #{categoryId}")
    long countTicketsByCategoryId(@Param("categoryId") Long categoryId);
}
