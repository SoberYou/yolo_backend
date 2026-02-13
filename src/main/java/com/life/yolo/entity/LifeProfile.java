package com.life.yolo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("life_profile")
public class LifeProfile {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate birthDate;

    private Integer expectedLifeYears;
    
    private Integer energyLifeYears;;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
