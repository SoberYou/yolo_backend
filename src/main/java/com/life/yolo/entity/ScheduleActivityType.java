package com.life.yolo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("schedule_activity_type")
public class ScheduleActivityType {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String typeCode;

    private String typeName;

    private String color;

    private Integer sort;

    private Integer enableFlag;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
