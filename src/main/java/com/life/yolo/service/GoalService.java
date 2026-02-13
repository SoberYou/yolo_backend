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

    private static final Long DEFAULT_USER_ID = 1L;

    public Goal saveGoal(Long id, String title, String description, Integer expectedTotalHours, String northStar, String status) {
        // Check title uniqueness
        QueryWrapper<Goal> query = new QueryWrapper<>();
        query.eq("title", title);
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
        } else {
            goal = new Goal();
            goal.setUserId(DEFAULT_USER_ID);
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

    public void deleteGoal(Long id) {
        goalMapper.deleteById(id);
    }

    public List<GoalWithStatsDto> getGoals(String status) {
        QueryWrapper<Goal> query = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            query.eq("status", status);
        } else {
            query.ne("status", "ARCHIVED");
        }
        query.orderByAsc("created_at");
        List<Goal> goals = goalMapper.selectList(query);

        // Get stats for last 7 days
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        QueryWrapper<FocusSession> sessionQuery = new QueryWrapper<>();
        sessionQuery.gt("start_date", sevenDaysAgo);
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
}
