package org.example.demo1.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.demo1.pojo.entity.RepairTicket;

import java.util.List;
import java.util.Map;

@Mapper
public interface RepairTicketMapper {

    @Select("SELECT id, user_id, category_id, category_name, title, description, location, contact_phone, image_url, status, reject_reason, handle_result, handler_id, accept_time, finish_time, cancel_time, create_time, update_time FROM repair_ticket WHERE id = #{id}")
    RepairTicket findById(@Param("id") Long id);

    @Select("SELECT id, user_id, category_id, category_name, title, description, location, contact_phone, image_url, status, reject_reason, handle_result, handler_id, accept_time, finish_time, cancel_time, create_time, update_time FROM repair_ticket WHERE id = #{id} FOR UPDATE")
    RepairTicket findByIdForUpdate(@Param("id") Long id);

    @Select("""
            <script>
            SELECT t.id, t.user_id, t.category_id, t.category_name, t.title, t.location, t.status, t.create_time, t.update_time
            FROM repair_ticket t
            WHERE t.user_id = #{userId}
            <if test='status != null and status != ""'>AND t.status = #{status}</if>
            ORDER BY t.create_time DESC
            </script>
            """)
    List<RepairTicket> findMine(@Param("userId") Long userId, @Param("status") String status);

    @Select("""
            <script>
            SELECT t.id, t.user_id, u.username, u.nickname, t.category_id, t.category_name, t.title, t.location, t.status, t.create_time, t.update_time
            FROM repair_ticket t
            INNER JOIN sys_user u ON u.id = t.user_id
            <where>
              <if test='status != null and status != ""'>AND t.status = #{status}</if>
              <if test='categoryId != null'>AND t.category_id = #{categoryId}</if>
              <if test='keyword != null and keyword != ""'>AND t.title LIKE CONCAT('%', #{keyword}, '%')</if>
            </where>
            ORDER BY t.create_time DESC
            </script>
            """)
    List<RepairTicket> findAdminList(@Param("status") String status, @Param("categoryId") Long categoryId, @Param("keyword") String keyword);

    @Select("""
            SELECT t.id, t.user_id, u.username, u.nickname, t.category_id, t.category_name, t.title, t.description, t.location,
                   t.contact_phone, t.image_url, t.status, t.reject_reason, t.handle_result, t.handler_id, h.nickname AS handler_name,
                   t.accept_time, t.finish_time, t.cancel_time, t.create_time, t.update_time
            FROM repair_ticket t
            INNER JOIN sys_user u ON u.id = t.user_id
            LEFT JOIN sys_user h ON h.id = t.handler_id
            WHERE t.id = #{id}
            """)
    RepairTicket findDetailById(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM repair_ticket WHERE user_id = #{userId} AND status IN ('PENDING','ACCEPTED','PROCESSING')")
    int countInProgressByUserId(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO repair_ticket (user_id, category_id, category_name, title, description, location, contact_phone, image_url, status)
            VALUES (#{userId}, #{categoryId}, #{categoryName}, #{title}, #{description}, #{location}, #{contactPhone}, #{imageUrl}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RepairTicket ticket);

    @Update("""
            UPDATE repair_ticket
            SET status = #{toStatus}, reject_reason = #{rejectReason}, handle_result = #{handleResult},
                handler_id = #{handlerId}, accept_time = #{acceptTime}, finish_time = #{finishTime},
                cancel_time = #{cancelTime}, update_time = NOW()
            WHERE id = #{id} AND status = #{fromStatus}
            """)
    int updateStatus(RepairTicket ticket);

    @Delete("DELETE FROM repair_ticket WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM repair_ticket")
    long countAll();

    @Select("SELECT status, COUNT(1) AS cnt FROM repair_ticket GROUP BY status")
    List<Map<String, Object>> countGroupByStatus();

    @Select("SELECT COUNT(1) FROM repair_ticket WHERE DATE(create_time) = CURDATE()")
    long countTodayNew();
}
