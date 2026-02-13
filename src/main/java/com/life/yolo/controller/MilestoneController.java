package com.life.yolo.controller;

import com.life.yolo.common.ApiResponse;
import com.life.yolo.entity.Milestone;
import com.life.yolo.service.MilestoneService;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/milestones")
public class MilestoneController {

    @Autowired
    private MilestoneService milestoneService;

    @PostMapping
    public ApiResponse<Milestone> saveMilestone(@RequestBody MilestoneRequest request) {
        Milestone milestone = new Milestone();
        BeanUtils.copyProperties(request, milestone);
        return ApiResponse.success(milestoneService.saveMilestone(milestone));
    }

    @PutMapping
    public ApiResponse<Milestone> updateMilestone(@RequestBody MilestoneRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("ID is required for update");
        }
        Milestone milestone = new Milestone();
        BeanUtils.copyProperties(request, milestone);
        return ApiResponse.success(milestoneService.saveMilestone(milestone));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMilestone(@PathVariable Integer id) {
        milestoneService.deleteMilestone(id);
        return ApiResponse.success(null);
    }

    @GetMapping
    public ApiResponse<List<Milestone>> getMilestoneList(@RequestParam(required = false) Long goalId) {
        return ApiResponse.success(milestoneService.getMilestoneList(goalId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Milestone> getMilestoneById(@PathVariable Integer id) {
        return ApiResponse.success(milestoneService.getMilestoneById(id));
    }

    @Data
    public static class MilestoneRequest {
        private Integer id;
        private Long goalId;
        private LocalDate milestoneDate;
        private String milestoneTitle;
        private String milestoneDesc;
        private String ownFeel;
    }
}
