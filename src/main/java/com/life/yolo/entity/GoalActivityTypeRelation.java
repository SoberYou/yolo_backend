package com.life.yolo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("goal_activity_type_relation")
public class GoalActivityTypeRelation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long goalId;

    private String typeCode;

    private LocalDateTime createTime;
}
