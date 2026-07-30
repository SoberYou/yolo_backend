package com.life.yolo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sop_template")
public class SopTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
