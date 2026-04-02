package com.life.yolo.controller;

import com.life.yolo.common.ApiResponse;
import com.life.yolo.entity.ScheduleActivityType;
import com.life.yolo.entity.ScheduleRecord;
import com.life.yolo.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // --- Schedule Activity Type APIs ---

    @PostMapping("/getActivityTypes")
    public ApiResponse<List<ScheduleActivityType>> getActivityTypes(
            @RequestParam(required = false) String typeName) {
        return ApiResponse.success(scheduleService.getActivityTypes(typeName));
    }

    @PostMapping("/createActivityType")
    public ApiResponse<ScheduleActivityType> createActivityType(@RequestBody ScheduleActivityType type) {
        return ApiResponse.success(scheduleService.createActivityType(type));
    }

    @PostMapping("/updateActivityType")
    public ApiResponse<ScheduleActivityType> updateActivityType(
            @RequestBody ScheduleActivityType type) {
        return ApiResponse.success(scheduleService.updateActivityType(type));
    }

    @PostMapping("/deleteActivityType")
    public ApiResponse<Void> deleteActivityType(@RequestParam Long id) {
        scheduleService.deleteActivityType(id);
        return ApiResponse.success(null);
    }

    // --- Schedule Record APIs ---

    @PostMapping("/getRecords")
    public ApiResponse<List<ScheduleRecord>> getRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bizDate,
            @RequestParam(required = false) String recordType) {
        return ApiResponse.success(scheduleService.getRecords(bizDate, recordType));
    }

    @PostMapping("/deleteRecord")
    public ApiResponse<Void> deleteRecord(@RequestParam Long id) {
        scheduleService.deleteRecord(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/batchSaveRecords")
    public ApiResponse<Void> batchSaveRecords(@RequestBody List<ScheduleRecord> records) {
        scheduleService.batchSaveRecords(records);
        return ApiResponse.success(null);
    }
}
