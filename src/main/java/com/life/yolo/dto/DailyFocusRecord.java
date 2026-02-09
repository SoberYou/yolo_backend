package com.life.yolo.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyFocusRecord {
    private LocalDate date;
    private Integer minutes;
}
