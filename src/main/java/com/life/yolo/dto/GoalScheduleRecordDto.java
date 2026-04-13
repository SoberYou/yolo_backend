package com.life.yolo.dto;

import lombok.Data;

@Data
public class GoalScheduleRecordDto {
    private Long goalId;
    private String startTime;
    private String endTime;
}
