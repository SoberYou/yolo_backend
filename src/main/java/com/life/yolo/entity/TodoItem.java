package com.life.yolo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("todo_item")
public class TodoItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String dateType;

    private String startDate;

    private String endDate;

    private String content;

    private String priority;

    private Integer sortOrder;

    private Integer isCompleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
