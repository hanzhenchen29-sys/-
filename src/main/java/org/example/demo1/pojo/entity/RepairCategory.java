package org.example.demo1.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RepairCategory {
    private Long id;
    private String name;
    private Integer status;
    private Integer sortNo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
