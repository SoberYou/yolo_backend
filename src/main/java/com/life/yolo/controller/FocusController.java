package com.life.yolo.controller;

import com.life.yolo.common.ApiResponse;
import com.life.yolo.dto.FocusSessionDto;
import com.life.yolo.dto.FocusStatsDto;
import com.life.yolo.entity.FocusSession;
import com.life.yolo.service.FocusService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/focus")
public class FocusController {

    @Autowired
    private FocusService focusService;

    @PostMapping("/start")
    public ApiResponse<FocusSession> startFocus(@RequestBody FocusStartRequest request) {
        return ApiResponse.success(focusService.startFocus(request.getGoalId()));
    }

    @PostMapping("/end")
    public ApiResponse<FocusSession> endFocus(@RequestBody(required = false) FocusEndRequest request) {
        if (request == null) {
            request = new FocusEndRequest();
        }
        return ApiResponse.success(focusService.endFocus(request.getId(), request.getStartTime(), request.getEndTime(), request.getDurationMinutes(), request.getMemo()));
    }

    @GetMapping("/running")
    public ApiResponse<FocusSession> getRunningSession(@RequestParam(required = false) Long goalId) {
        return ApiResponse.success(focusService.getRunningSession(goalId));
    }

    @GetMapping("/statistics")
    public ApiResponse<FocusStatsDto> getStatistics(@RequestParam Long goalId) {
        return ApiResponse.success(focusService.getStatistics(goalId));
    }

    @GetMapping
    public ApiResponse<List<FocusSessionDto>> getFocusList(@RequestParam(required = false) Long goalId) {
        return ApiResponse.success(focusService.getFocusSessionList(goalId));
    }

    @GetMapping("/{id}")
    public ApiResponse<FocusSessionDto> getFocusById(@PathVariable Long id) {
        return ApiResponse.success(focusService.getFocusSessionById(id));
    }

    @Data
    public static class FocusStartRequest {
        private Long goalId;
    }

    @Data
    public static class FocusEndRequest {
        private Long id;
        private java.time.LocalDateTime startTime;
        private java.time.LocalDateTime endTime;
        private String memo;
        private Integer durationMinutes;
    }
}
