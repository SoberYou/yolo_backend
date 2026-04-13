package com.life.yolo.dto;

import lombok.Data;

import java.util.List;

@Data
public class GoalActivityRelationDto {
    private Long goalId;
    private List<String> typeCodes;
}