package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.dto.GoalWithStatsDto;
import com.life.yolo.entity.FocusSession;
import com.life.yolo.entity.Goal;
import com.life.yolo.mapper.FocusSessionMapper;
import com.life.yolo.mapper.GoalMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.life.yolo.mapper.GoalActivityTypeRelationMapper;
import com.life.yolo.entity.GoalActivityTypeRelation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoalService {

    @Autowired
    private GoalMapper goalMapper;

    @Autowired
    private FocusSessionMapper focusSessionMapper;

    @Autowired
    private GoalActivityTypeRelationMapper goalActivityTypeRelationMapper;

    @Autowired
    private UserService userService;

    public Goal saveGoal(Long userId, Long id, String title, String description, Integer expectedTotalHours, String northStar, String status) {
        userService.getInternalUserId(userId); // Validate user existence

        // Check title uniqueness
        QueryWrapper<Goal> query = new QueryWrapper<>();
        query.eq("title", title);
        query.eq("user_id", userId); // Check uniqueness within user scope
        if (id != null) {
            query.ne("id", id);
        }
        if (goalMapper.selectCount(query) > 0) {
            throw new IllegalArgumentException("Goal title already exists");
        }

        Goal goal;
        if (id != null) {
            goal = goalMapper.selectById(id);
            if (goal == null) {
                throw new IllegalArgumentException("Goal not found with id: " + id);
            }
            if (!goal.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Goal does not belong to user");
            }
        } else {
            goal = new Goal();
            goal.setUserId(userId);
            goal.setStatus(status != null ? status : "ACTIVE");
            goal.setCreatedAt(LocalDateTime.now());
        }

        goal.setTitle(title);
        goal.setDescription(description);
        goal.setExpectedTotalHours(expectedTotalHours);
        goal.setNorthStar(northStar);
        if (status != null) {
            goal.setStatus(status);
        }
        goal.setUpdatedAt(LocalDateTime.now());
        
        if (id != null) {
            goalMapper.updateById(goal);
        } else {
            goalMapper.insert(goal);
        }
        return goal;
    }

    public void deleteGoal(Long userId, Long id) {
        userService.getInternalUserId(userId);
        Goal goal = goalMapper.selectById(id);
        if (goal != null && goal.getUserId().equals(userId)) {
            goalMapper.deleteById(id);
        } else {
             throw new IllegalArgumentException("Goal not found or access denied");
        }
    }

    public List<GoalWithStatsDto> getGoals(Long userId, String status) {
        userService.getInternalUserId(userId);

        QueryWrapper<Goal> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        if (status != null && !status.isEmpty()) {
            query.eq("status", status);
        } else {
            query.ne("status", "ARCHIVED");
        }
        query.orderByDesc("created_at");
        List<Goal> goals = goalMapper.selectList(query);

        // Get stats for last 7 days
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(7).atStartOfDay();
        QueryWrapper<FocusSession> sessionQuery = new QueryWrapper<>();
        sessionQuery.gt("start_time", sevenDaysAgo);
        sessionQuery.eq("status", "COMPLETED");
        
        List<FocusSession> recentSessions = focusSessionMapper.selectList(sessionQuery);
        
        // Group by goalId and sum minutes
        Map<Long, Long> statsMap = recentSessions.stream()
                .filter(s -> s.getGoalId() != null && s.getDurationMinutes() != null)
                .collect(Collectors.groupingBy(
                        FocusSession::getGoalId,
                        Collectors.summingLong(FocusSession::getDurationMinutes)
                ));

        // Convert to DTO
        return goals.stream().map(goal -> {
            GoalWithStatsDto dto = new GoalWithStatsDto();
            BeanUtils.copyProperties(goal, dto);
            dto.setLast7DaysMinutes(statsMap.getOrDefault(goal.getId(), 0L));
            return dto;
        }).collect(Collectors.toList());
    }

    public Goal getGoalById(Long goalId) {
        return goalMapper.selectById(goalId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void configureGoalActivityRelations(Long userId, Long goalId, List<String> typeCodes) {
        userService.getInternalUserId(userId);
        
        Goal goal = goalMapper.selectById(goalId);
        if (goal == null || !goal.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Goal not found or access denied");
        }

        // 1. 删除旧的关联关系
        QueryWrapper<GoalActivityTypeRelation> deleteQuery = new QueryWrapper<>();
        deleteQuery.eq("user_id", userId);
        deleteQuery.eq("goal_id", goalId);
        goalActivityTypeRelationMapper.delete(deleteQuery);

        // 2. 插入新的关联关系
        if (typeCodes != null && !typeCodes.isEmpty()) {
            for (String typeCode : typeCodes) {
                GoalActivityTypeRelation relation = new GoalActivityTypeRelation();
                relation.setUserId(userId);
                relation.setGoalId(goalId);
                relation.setTypeCode(typeCode);
                relation.setCreateTime(LocalDateTime.now());
                goalActivityTypeRelationMapper.insert(relation);
            }
        }
    }

    public List<String> getGoalActivityRelations(Long userId, Long goalId) {
        userService.getInternalUserId(userId);
        
        Goal goal = goalMapper.selectById(goalId);
        if (goal == null || !goal.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Goal not found or access denied");
        }

        QueryWrapper<GoalActivityTypeRelation> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.eq("goal_id", goalId);
        
        List<GoalActivityTypeRelation> relations = goalActivityTypeRelationMapper.selectList(query);
        return relations.stream()
                .map(GoalActivityTypeRelation::getTypeCode)
                .collect(Collectors.toList());
    }

    public void addGoalActivityRelation(Long userId, Long goalId, String typeCode) {
        userService.getInternalUserId(userId);
        
        Goal goal = goalMapper.selectById(goalId);
        if (goal == null || !goal.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Goal not found or access denied");
        }

        // 检查是否已存在
        QueryWrapper<GoalActivityTypeRelation> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.eq("goal_id", goalId);
        query.eq("type_code", typeCode);
        
        if (goalActivityTypeRelationMapper.selectCount(query) == 0) {
            GoalActivityTypeRelation relation = new GoalActivityTypeRelation();
            relation.setUserId(userId);
            relation.setGoalId(goalId);
            relation.setTypeCode(typeCode);
            relation.setCreateTime(LocalDateTime.now());
            goalActivityTypeRelationMapper.insert(relation);
        }
    }

    public void deleteGoalActivityRelation(Long userId, Long goalId, String typeCode) {
        userService.getInternalUserId(userId);
        
        Goal goal = goalMapper.selectById(goalId);
        if (goal == null || !goal.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Goal not found or access denied");
        }

        QueryWrapper<GoalActivityTypeRelation> deleteQuery = new QueryWrapper<>();
        deleteQuery.eq("user_id", userId);
        deleteQuery.eq("goal_id", goalId);
        deleteQuery.eq("type_code", typeCode);
        
        goalActivityTypeRelationMapper.delete(deleteQuery);
    }
}
