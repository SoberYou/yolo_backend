package com.life.yolo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("goal")
public class Goal {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
