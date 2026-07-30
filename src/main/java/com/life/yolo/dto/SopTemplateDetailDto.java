package com.life.yolo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SopTemplateDetailDto {
    private Long id;
    private Long userId;
    private String name;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<SopCategoryDto> categories;
}
