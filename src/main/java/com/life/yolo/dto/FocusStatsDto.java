package com.life.yolo.dto;

import lombok.Data;
import java.util.List;

@Data
public class FocusStatsDto {
    private Long goalId;
    private String goalTitle;
    private Long last7DaysMinutes;
    private Long last30DaysMinutes;
    private List<DailyFocusRecord> dailyRecords;
}
