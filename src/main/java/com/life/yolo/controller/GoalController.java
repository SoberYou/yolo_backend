package com.life.yolo.controller;

import com.life.yolo.common.ApiResponse;
import com.life.yolo.dto.GoalWithStatsDto;
import com.life.yolo.dto.GoalActivityRelationDto;
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
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        return ApiResponse.success(goalService.saveGoal(
                request.getUserId(),
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getExpectedTotalHours(),
                request.getNorthStar(),
                request.getStatus()
        ));
    }

    @GetMapping
    public ApiResponse<List<GoalWithStatsDto>> getGoals(@RequestParam Long userId, @RequestParam(required = false) String status) {
        return ApiResponse.success(goalService.getGoals(userId, status));
    }

    @GetMapping("/{goalId}")
    public ApiResponse<Goal> getGoal(@PathVariable Long goalId) {
        return ApiResponse.success(goalService.getGoalById(goalId));
    }

    @DeleteMapping("/{goalId}")
    public ApiResponse<Void> deleteGoal(@PathVariable Long goalId, @RequestParam Long userId) {
        goalService.deleteGoal(userId, goalId);
        return ApiResponse.success(null);
    }

    @Data
    public static class GoalSaveRequest {
        private Long id;
        private Long userId;
        private String title;
        private String description;
        private Integer expectedTotalHours;
        private String northStar;
        private String status;
    }

    @PostMapping("/configureGoalActivityRelations")
    public ApiResponse<Void> configureGoalActivityRelations(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody GoalActivityRelationDto dto) {
        goalService.configureGoalActivityRelations(userId, dto.getGoalId(), dto.getTypeCodes());
        return ApiResponse.success(null);
    }

    @GetMapping("/getGoalActivityRelations/{goalId}")
    public ApiResponse<List<String>> getGoalActivityRelations(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long goalId) {
        List<String> typeCodes = goalService.getGoalActivityRelations(userId, goalId);
        return ApiResponse.success(typeCodes);
    }

    @PostMapping("/addGoalActivityRelation/{goalId}/{typeCode}")
    public ApiResponse<Void> addGoalActivityRelation(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long goalId,
            @PathVariable String typeCode) {
        goalService.addGoalActivityRelation(userId, goalId, typeCode);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/deleteGoalActivityRelation/{goalId}/{typeCode}")
    public ApiResponse<Void> deleteGoalActivityRelation(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long goalId,
            @PathVariable String typeCode) {
        goalService.deleteGoalActivityRelation(userId, goalId, typeCode);
        return ApiResponse.success(null);
    }
}
