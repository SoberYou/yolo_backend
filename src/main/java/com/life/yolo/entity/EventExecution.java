package com.life.yolo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("event_execution")
public class EventExecution {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scheduleId;

    private LocalDate executeDate;

    private Integer isExecuted;

    private LocalDateTime executedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
