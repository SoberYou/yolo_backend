package com.life.yolo.dto;

import com.life.yolo.entity.Goal;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoalWithStatsDto extends Goal {
    private Long last7DaysMinutes;
    private List<String> typeCodes;
}
