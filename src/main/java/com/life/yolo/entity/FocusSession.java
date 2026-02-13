package com.life.yolo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("focus_session")
public class FocusSession {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goalId;

    private LocalDate startDate;
    
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private Integer durationMinutes;

    private String memo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
