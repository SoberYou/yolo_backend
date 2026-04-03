package com.life.yolo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("schedule_record")
public class ScheduleRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate bizDate;

    private String recordType;

    private String startTime;

    private String endTime;

    private String activityType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
