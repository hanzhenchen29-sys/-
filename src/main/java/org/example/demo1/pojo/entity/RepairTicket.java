package org.example.demo1.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RepairTicket {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private String location;
    private String contactPhone;
    private String imageUrl;
    private String status;
    private String rejectReason;
    private String handleResult;
    private Long handlerId;
    private LocalDateTime acceptTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String username;
    private String nickname;
    private String handlerName;

    /** 状态更新时的期望原状态（CAS） */
    private String fromStatus;
    /** 状态更新时的目标状态 */
    private String toStatus;
}
