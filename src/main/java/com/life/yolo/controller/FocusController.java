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
        return ApiResponse.success(focusService.startFocus(request.getUserId(), request.getGoalId()));
    }

    @PostMapping("/end")
    public ApiResponse<FocusSession> endFocus(@RequestBody(required = false) FocusEndRequest request) {
        if (request == null) {
            request = new FocusEndRequest();
        }
        return ApiResponse.success(focusService.endFocus(request.getUserId(), request.getId(), request.getStartTime(), request.getEndTime(), request.getDurationMinutes(), request.getMemo()));
    }

    @GetMapping("/running")
    public ApiResponse<FocusSession> getRunningSession(@RequestParam Long userId, @RequestParam(required = false) Long goalId) {
        return ApiResponse.success(focusService.getRunningSession(userId, goalId));
    }

    @GetMapping("/statistics")
    public ApiResponse<FocusStatsDto> getStatistics(@RequestParam Long userId, @RequestParam Long goalId) {
        return ApiResponse.success(focusService.getStatistics(userId, goalId));
    }

    @GetMapping
    public ApiResponse<List<FocusSessionDto>> getFocusList(@RequestParam Long userId, @RequestParam(required = false) Long goalId) {
        return ApiResponse.success(focusService.getFocusSessionList(userId, goalId));
    }

    @GetMapping("/{id}")
    public ApiResponse<FocusSessionDto> getFocusById(@RequestParam Long userId, @PathVariable Long id) {
        return ApiResponse.success(focusService.getFocusSessionById(userId, id));
    }

    @Data
    public static class FocusStartRequest {
        private Long userId;
        private Long goalId;
    }

    @Data
    public static class FocusEndRequest {
        private Long userId;
        private Long id;
        private java.time.LocalDateTime startTime;
        private java.time.LocalDateTime endTime;
        private String memo;
        private Integer durationMinutes;
    }
}
