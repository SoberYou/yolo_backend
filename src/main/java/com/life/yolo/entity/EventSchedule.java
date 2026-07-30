package com.life.yolo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("event_schedule")
public class EventSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String eventName;

    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDate effectiveStartDate;

    private LocalDate effectiveEndDate;

    private Integer isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
