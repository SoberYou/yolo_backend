package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.dto.DailyFocusRecord;
import com.life.yolo.dto.FocusStatsDto;
import com.life.yolo.entity.FocusSession;
import com.life.yolo.entity.Goal;
import com.life.yolo.mapper.FocusSessionMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.yolo.dto.FocusSessionDto;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FocusService {

    @Autowired
    private FocusSessionMapper focusSessionMapper;

    @Autowired
    private GoalService goalService;

    @Transactional(rollbackFor = Exception.class)
    public FocusSession startFocus(Long goalId) {
        // Check if there is any running session
        QueryWrapper<FocusSession> query = new QueryWrapper<>();
        query.eq("goal_id", goalId);
        query.eq("status", "RUNNING");
        Long count = focusSessionMapper.selectCount(query);
        
        if (count > 0) {
            throw new RuntimeException("There is already a running focus session");
        }

        FocusSession session = new FocusSession();
        session.setGoalId(goalId);
        session.setStartDate(LocalDate.now());
        session.setStartTime(LocalDateTime.now());
        session.setStatus("RUNNING");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        
        focusSessionMapper.insert(session);
        return session;
    }

    @Transactional(rollbackFor = Exception.class)
    public FocusSession endFocus(Long id, LocalDateTime startTime, LocalDateTime endTime, Integer durationMinutes, String memo) {
        FocusSession session;
        if (id != null) {
            session = focusSessionMapper.selectById(id);
            if (session == null) {
                throw new RuntimeException("Focus session not found with id: " + id);
            }
        } else {
            QueryWrapper<FocusSession> query = new QueryWrapper<>();
            query.eq("status", "RUNNING");
            session = focusSessionMapper.selectOne(query);
            if (session == null) {
                throw new RuntimeException("No running focus session found");
            }
        }

        if (startTime != null) {
            session.setStartTime(startTime);
        }

        LocalDateTime finalEndTime;
        if (durationMinutes != null) {
            finalEndTime = session.getStartTime().plusMinutes(durationMinutes);
            session.setDurationMinutes(durationMinutes);
        } else {
            if (endTime != null) {
                finalEndTime = endTime;
            } else {
                finalEndTime = LocalDateTime.now();
            }
            long calculatedMinutes = ChronoUnit.MINUTES.between(session.getStartTime(), finalEndTime);
            session.setDurationMinutes((int) calculatedMinutes);
        }

        if (memo != null) {
            session.setMemo(memo);
        }

        session.setEndTime(finalEndTime);
        session.setStatus("COMPLETED");
        session.setUpdatedAt(LocalDateTime.now());
        
        focusSessionMapper.updateById(session);
        return session;
    }

    public FocusStatsDto getStatistics(Long goalId) {
        if (goalId == null) {
            throw new IllegalArgumentException("Goal ID is required");
        }

        Goal goal = goalService.getGoalById(goalId);
        if (goal == null) {
            throw new RuntimeException("Goal not found");
        }

        QueryWrapper<FocusSession> query = new QueryWrapper<>();
        query.eq("goal_id", goalId);
        query.eq("status", "COMPLETED");
        query.orderByDesc("start_time");
        
        List<FocusSession> sessions = focusSessionMapper.selectList(query);
        
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        LocalDate thirtyDaysAgo = today.minusDays(30);

        long totalMinutes = sessions.stream()
                .mapToLong(s -> s.getDurationMinutes() != null ? s.getDurationMinutes() : 0)
                .sum();
        
        long last7DaysMinutes = sessions.stream()
                .filter(s -> s.getStartDate() != null && s.getStartDate().isAfter(sevenDaysAgo))
                .mapToLong(s -> s.getDurationMinutes() != null ? s.getDurationMinutes() : 0)
                .sum();

        long last30DaysMinutes = sessions.stream()
                .filter(s -> s.getStartDate() != null && s.getStartDate().isAfter(thirtyDaysAgo))
                .mapToLong(s -> s.getDurationMinutes() != null ? s.getDurationMinutes() : 0)
                .sum();

        // Group by date for daily records
        Map<LocalDate, Integer> dailyMap = sessions.stream()
                .filter(s -> s.getStartTime() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getStartTime().toLocalDate(),
                        Collectors.summingInt(s -> s.getDurationMinutes() != null ? s.getDurationMinutes() : 0)
                ));

        List<DailyFocusRecord> dailyRecords = dailyMap.entrySet().stream()
                .map(entry -> new DailyFocusRecord(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DailyFocusRecord::getDate).reversed())
                .collect(Collectors.toList());
        
        FocusStatsDto dto = new FocusStatsDto();
        dto.setGoalId(goalId);
        dto.setGoalTitle(goal.getTitle());
        dto.setTotalMinutes(totalMinutes);
        dto.setLast7DaysMinutes(last7DaysMinutes);
        dto.setLast30DaysMinutes(last30DaysMinutes);
        dto.setDailyRecords(dailyRecords);
        
        return dto;
    }

    public FocusSession getRunningSession(Long goalId) {
        QueryWrapper<FocusSession> query = new QueryWrapper<>();
        query.eq("status", "RUNNING");
        if (goalId != null) {
            query.eq("goal_id", goalId);
        }
        return focusSessionMapper.selectOne(query);
    }

    public FocusSessionDto getFocusSessionById(Long id) {
        FocusSession session = focusSessionMapper.selectById(id);
        if (session == null) {
            throw new RuntimeException("Focus session not found with id: " + id);
        }

        FocusSessionDto dto = new FocusSessionDto();
        BeanUtils.copyProperties(session, dto);

        if (session.getGoalId() != null) {
            Goal goal = goalService.getGoalById(session.getGoalId());
            if (goal != null) {
                dto.setGoalTitle(goal.getTitle());
            }
        }

        return dto;
    }

    public List<FocusSessionDto> getFocusSessionList(Long goalId) {
        QueryWrapper<FocusSession> query = new QueryWrapper<>();
        if (goalId != null) {
            query.eq("goal_id", goalId);
        }
        query.orderByDesc("start_time");

        List<FocusSession> sessions = focusSessionMapper.selectList(query);
        
        // Get all unique goal IDs
        List<Long> goalIds = sessions.stream()
                .map(FocusSession::getGoalId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> goalTitleMap = new java.util.HashMap<>();
        if (!goalIds.isEmpty()) {
            // Since we don't have batch get in GoalService (only getGoalById), we can fetch individually or add a method.
            // For now, let's fetch individually as it's MVP and simpler, or assume we can rely on GoalService.
            // Actually, we can just loop.
            for (Long gid : goalIds) {
                Goal g = goalService.getGoalById(gid);
                if (g != null) {
                    goalTitleMap.put(gid, g.getTitle());
                }
            }
        }

        return sessions.stream().map(session -> {
            FocusSessionDto dto = new FocusSessionDto();
            BeanUtils.copyProperties(session, dto);
            dto.setGoalTitle(goalTitleMap.get(session.getGoalId()));
            return dto;
        }).collect(Collectors.toList());
    }
}
