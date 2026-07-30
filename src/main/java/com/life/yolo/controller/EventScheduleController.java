package com.life.yolo.controller;

import com.life.yolo.common.ApiResponse;
import com.life.yolo.entity.EventSchedule;
import com.life.yolo.service.EventScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventSchedule")
@RequiredArgsConstructor
public class EventScheduleController {

    private final EventScheduleService eventScheduleService;

    @GetMapping("/active")
    public ApiResponse<List<EventSchedule>> getActiveEvents(@RequestParam Long userId) {
        return ApiResponse.success(eventScheduleService.listActiveEvents(userId));
    }

    @GetMapping("/archive")
    public ApiResponse<List<EventSchedule>> getArchivedEvents(@RequestParam Long userId) {
        return ApiResponse.success(eventScheduleService.listArchivedEvents(userId));
    }

    @PostMapping("/create")
    public ApiResponse<EventSchedule> createEvent(@RequestParam Long userId, @RequestBody EventSchedule payload) {
        return ApiResponse.success(eventScheduleService.createEvent(userId, payload));
    }

    @PostMapping("/{scheduleId}/rename")
    public ApiResponse<EventSchedule> renameEvent(@RequestParam Long userId,
                                                  @PathVariable Long scheduleId,
                                                  @RequestBody Map<String, String> payload) {
        return ApiResponse.success(eventScheduleService.renameEvent(userId, scheduleId, payload.get("eventName")));
    }

    @PostMapping("/{scheduleId}/archive")
    public ApiResponse<EventSchedule> archiveEvent(@RequestParam Long userId,
                                                   @PathVariable Long scheduleId,
                                                   @RequestBody Map<String, String> payload) {
        LocalDate effectiveEndDate = payload.get("effectiveEndDate") == null ? null : LocalDate.parse(payload.get("effectiveEndDate"));
        return ApiResponse.success(eventScheduleService.archiveEvent(userId, scheduleId, effectiveEndDate));
    }

    @PostMapping("/{scheduleId}/delete")
    public ApiResponse<Void> deleteEvent(@RequestParam Long userId, @PathVariable Long scheduleId) {
        eventScheduleService.deleteEvent(userId, scheduleId);
        return ApiResponse.success(null);
    }

    @GetMapping("/view")
    public ApiResponse<Map<String, Object>> getScheduleView(@RequestParam Long userId,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(eventScheduleService.getScheduleView(userId, startDate, endDate));
    }

    @PostMapping("/execution/toggle")
    public ApiResponse<Map<String, Object>> toggleExecution(@RequestParam Long userId,
                                                            @RequestBody Map<String, String> payload) {
        Long scheduleId = Long.valueOf(payload.get("scheduleId"));
        LocalDate executeDate = LocalDate.parse(payload.get("executeDate"));
        return ApiResponse.success(eventScheduleService.toggleExecution(userId, scheduleId, executeDate));
    }

    @PostMapping("/execution/updateTime")
    public ApiResponse<Map<String, Object>> updateExecutionTime(@RequestParam Long userId,
                                                                @RequestBody Map<String, String> payload) {
        Long scheduleId = Long.valueOf(payload.get("scheduleId"));
        LocalDate executeDate = LocalDate.parse(payload.get("executeDate"));
        LocalTime executedAt = LocalTime.parse(payload.get("executedAt"));
        return ApiResponse.success(eventScheduleService.updateExecutionTime(userId, scheduleId, executeDate, executedAt));
    }
}
