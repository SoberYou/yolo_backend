package com.life.yolo.controller;

import com.life.yolo.common.ApiResponse;
import com.life.yolo.dto.FocusStatsDto;
import com.life.yolo.entity.FocusSession;
import com.life.yolo.service.FocusService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<FocusSession> endFocus() {
        return ApiResponse.success(focusService.endFocus());
    }

    @GetMapping("/statistics")
    public ApiResponse<FocusStatsDto> getStatistics(@RequestParam Long goalId) {
        return ApiResponse.success(focusService.getStatistics(goalId));
    }

    @Data
    public static class FocusStartRequest {
        private Long goalId;
    }
}
