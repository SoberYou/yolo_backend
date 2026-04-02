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
            @RequestParam Long userId,
            @RequestParam(required = false) String typeName) {
        return ApiResponse.success(scheduleService.getActivityTypes(userId, typeName));
    }

    @PostMapping("/createActivityType")
    public ApiResponse<ScheduleActivityType> createActivityType(
            @RequestParam Long userId,
            @RequestBody ScheduleActivityType type) {
        return ApiResponse.success(scheduleService.createActivityType(type, userId));
    }

    @PostMapping("/updateActivityType")
    public ApiResponse<ScheduleActivityType> updateActivityType(
            @RequestParam Long userId,
            @RequestBody ScheduleActivityType type) {
        return ApiResponse.success(scheduleService.updateActivityType(type, userId));
    }

    @PostMapping("/deleteActivityType")
    public ApiResponse<Void> deleteActivityType(
            @RequestParam Long id,
            @RequestParam Long userId) {
        scheduleService.deleteActivityType(id, userId);
        return ApiResponse.success(null);
    }

    // --- Schedule Record APIs ---

    @PostMapping("/getRecords")
    public ApiResponse<List<ScheduleRecord>> getRecords(
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String recordType) {
        return ApiResponse.success(scheduleService.getRecords(userId, startDate, endDate, recordType));
    }

    @PostMapping("/deleteRecord")
    public ApiResponse<Void> deleteRecord(
            @RequestParam Long id,
            @RequestParam Long userId) {
        scheduleService.deleteRecord(id, userId);
        return ApiResponse.success(null);
    }

    @PostMapping("/batchSaveRecords")
    public ApiResponse<Void> batchSaveRecords(
            @RequestParam Long userId,
            @RequestBody List<ScheduleRecord> records) {
        scheduleService.batchSaveRecords(userId, records);
        return ApiResponse.success(null);
    }
}
