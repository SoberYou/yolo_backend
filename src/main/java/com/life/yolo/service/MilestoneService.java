package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.entity.Goal;
import com.life.yolo.entity.Milestone;
import com.life.yolo.mapper.GoalMapper;
import com.life.yolo.mapper.MilestoneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MilestoneService {

    @Autowired
    private MilestoneMapper milestoneMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private GoalMapper goalMapper;

    @Transactional(rollbackFor = Exception.class)
    public Milestone saveMilestone(Milestone milestone, Long userId) {
        userService.getInternalUserId(userId);

        // Verify goal ownership
        if (milestone.getGoalId() != null) {
            Goal goal = goalMapper.selectById(milestone.getGoalId());
            if (goal == null) {
                throw new IllegalArgumentException("Goal not found with id: " + milestone.getGoalId());
            }
            if (!goal.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Access denied for goal: " + milestone.getGoalId());
            }
        } else if (milestone.getId() == null) {
             throw new IllegalArgumentException("Goal ID is required for new milestone");
        } else {
            // Updating existing milestone, check if goalId is changed or just check existing ownership
            Milestone existing = milestoneMapper.selectById(milestone.getId());
            if (existing == null) {
                 throw new IllegalArgumentException("Milestone not found");
            }
            Goal goal = goalMapper.selectById(existing.getGoalId());
             if (goal == null || !goal.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Access denied for milestone");
            }
            // If goalId is not in the update object, keep the old one
            milestone.setGoalId(existing.getGoalId());
        }

        if (milestone.getId() == null) {
            milestone.setCreateAt(LocalDateTime.now());
            milestone.setUpdateAt(LocalDateTime.now());
            milestoneMapper.insert(milestone);
        } else {
            milestone.setUpdateAt(LocalDateTime.now());
            milestoneMapper.updateById(milestone);
            return milestoneMapper.selectById(milestone.getId());
        }
        return milestone;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMilestone(Integer id, Long userId) {
        userService.getInternalUserId(userId);
        Milestone milestone = milestoneMapper.selectById(id);
        if (milestone == null) {
             throw new IllegalArgumentException("Milestone not found");
        }
        Goal goal = goalMapper.selectById(milestone.getGoalId());
        if (goal == null || !goal.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }
        milestoneMapper.deleteById(id);
    }

    public List<Milestone> getMilestoneList(Long goalId, Long userId) {
        userService.getInternalUserId(userId);
        QueryWrapper<Milestone> query = new QueryWrapper<>();
        
        if (goalId != null) {
            Goal goal = goalMapper.selectById(goalId);
            if (goal == null || !goal.getUserId().equals(userId)) {
                 throw new IllegalArgumentException("Access denied for goal");
            }
            query.eq("goal_id", goalId);
        } else {
            // Get all goals for user
            QueryWrapper<Goal> goalQuery = new QueryWrapper<>();
            goalQuery.eq("user_id", userId);
            List<Goal> goals = goalMapper.selectList(goalQuery);
            if (goals.isEmpty()) {
                return List.of();
            }
            List<Long> goalIds = goals.stream().map(Goal::getId).collect(Collectors.toList());
            query.in("goal_id", goalIds);
        }
        
        query.orderByDesc("milestone_date", "create_at");
        return milestoneMapper.selectList(query);
    }

    public Milestone getMilestoneById(Integer id, Long userId) {
        userService.getInternalUserId(userId);
        Milestone milestone = milestoneMapper.selectById(id);
        if (milestone == null) {
            throw new IllegalArgumentException("Milestone not found");
        }
        Goal goal = goalMapper.selectById(milestone.getGoalId());
        if (goal == null || !goal.getUserId().equals(userId)) {
             throw new IllegalArgumentException("Access denied");
        }
        return milestone;
    }
}
