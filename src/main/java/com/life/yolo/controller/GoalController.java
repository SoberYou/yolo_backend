package com.life.yolo.controller;

import com.life.yolo.common.ApiResponse;
import com.life.yolo.dto.GoalWithStatsDto;
import com.life.yolo.entity.Goal;
import com.life.yolo.service.GoalService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @PostMapping
    public ApiResponse<Goal> saveGoal(@RequestBody GoalSaveRequest request) {
        Goal goal = goalService.saveGoal(request.getId(), request.getTitle(), request.getDescription(),request.getExpectedTotalHours(), request.getNorthStar(), request.getStatus());
        return ApiResponse.success(goal);
    }

    @DeleteMapping("/{goalId}")
    public ApiResponse<Void> deleteGoal(@PathVariable Long goalId) {
        goalService.deleteGoal(goalId);
        return ApiResponse.success(null);
    }

    @GetMapping
    public ApiResponse<List<GoalWithStatsDto>> getGoals(@RequestParam(required = false) String status) {
        return ApiResponse.success(goalService.getGoals(status));
    }

    @GetMapping("/{goalId}")
    public ApiResponse<Goal> getGoal(@PathVariable Long goalId) {
        return ApiResponse.success(goalService.getGoalById(goalId));
    }

    @Data
    public static class GoalSaveRequest {
        private Long id;
        private String title;
        private String description;
        private Integer expectedTotalHours;
        private String northStar;
        private String status;
    }
}
