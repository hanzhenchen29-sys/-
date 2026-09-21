package org.example.demo1.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.example.demo1.pojo.entity.RepairTicketLog;

@Mapper
public interface RepairTicketLogMapper {

    @Insert("""
            INSERT INTO repair_ticket_log (ticket_id, operator_id, from_status, to_status, action, content)
            VALUES (#{ticketId}, #{operatorId}, #{fromStatus}, #{toStatus}, #{action}, #{content})
            """)
    int insert(RepairTicketLog log);

    @Delete("DELETE FROM repair_ticket_log WHERE ticket_id = #{ticketId}")
    int deleteByTicketId(Long ticketId);
}
