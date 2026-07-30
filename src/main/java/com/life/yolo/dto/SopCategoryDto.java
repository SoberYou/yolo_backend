package com.life.yolo.dto;

import com.life.yolo.entity.SopItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SopCategoryDto {
    private Long id;
    private Long templateId;
    private Long userId;
    private String name;
    private String type;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<SopItem> items;
}
