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

    public Goal createGoal(String title, String description, Integer expectedTotalHours) {
        Goal goal = new Goal();
        goal.setUserId(DEFAULT_USER_ID);
        goal.setTitle(title);
        goal.setDescription(description);
        goal.setExpectedTotalHours(expectedTotalHours);
        goal.setStatus("ACTIVE");
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        
        goalMapper.insert(goal);
        return goal;
    }

    public List<GoalWithStatsDto> getGoals() {
        QueryWrapper<Goal> query = new QueryWrapper<>();
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
