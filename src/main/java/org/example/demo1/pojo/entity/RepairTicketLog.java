package org.example.demo1.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RepairTicketLog {
    private Long id;
    private Long ticketId;
    private Long operatorId;
    private String fromStatus;
    private String toStatus;
    private String action;
    private String content;
    private LocalDateTime createTime;
}
