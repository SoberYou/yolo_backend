package com.life.yolo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sop_item")
public class SopItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private Long userId;
    private String itemKey;
    private String itemValue;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
