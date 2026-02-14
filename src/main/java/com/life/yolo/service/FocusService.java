package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.dto.DailyFocusRecord;
import com.life.yolo.dto.FocusStatsDto;
import com.life.yolo.dto.GoalWithStatsDto;
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

    @Autowired
    private UserService userService;

    @Transactional(rollbackFor = Exception.class)
    public FocusSession startFocus(Long userId, Long goalId) {
        userService.getInternalUserId(userId);
        
        // Check if goal belongs to user
        Goal goal = goalService.getGoalById(goalId);
        if (goal == null || !goal.getUserId().equals(userId)) {
            throw new RuntimeException("Goal not found or access denied");
        }

        // Check if there is any running session
        QueryWrapper<FocusSession> query = new QueryWrapper<>();
        query.eq("goal_id", goalId); // This limits to the specific goal. Should we limit per user?
        // Ideally, a user can only have one running session globally, regardless of goal.
        // Let's check global running session for this user.
        // Since FocusSession table doesn't have user_id, we need to join or check all goals of user?
        // Or simpler: The requirements don't strictly say "one session globally", but it's implied for "Focus".
        // However, current schema only links Session -> Goal.
        // Let's stick to the existing logic but ensure we check ownership.
        
        // Wait, if I want to ensure NO running session for the USER, I need to check all sessions for goals owned by user?
        // That might be expensive if not indexed.
        // For MVP, let's just check the current goal's running session as per original code, 
        // OR better: check if there's any running session for THIS goal (as per original code).
        // Original code: query.eq("goal_id", goalId); query.eq("status", "RUNNING");
        // I will keep this behavior for now to minimize logic change risk, just adding ownership check.
        
        query.eq("status", "RUNNING");
        Long count = focusSessionMapper.selectCount(query);
        
        if (count > 0) {
            throw new RuntimeException("There is already a running focus session for this goal");
        }

        FocusSession session = new FocusSession();
        session.setGoalId(goalId);
        session.setStartTime(LocalDateTime.now());
        session.setStatus("RUNNING");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        
        focusSessionMapper.insert(session);
        return session;
    }

    @Transactional(rollbackFor = Exception.class)
    public FocusSession endFocus(Long userId, Long id, LocalDateTime startTime, LocalDateTime endTime, Integer durationMinutes, String memo) {
        userService.getInternalUserId(userId);
        FocusSession session;
        if (id != null) {
            session = focusSessionMapper.selectById(id);
            if (session == null) {
                throw new RuntimeException("Focus session not found with id: " + id);
            }
        } else {
            // Find running session for user's goals
            // Since we don't have user_id in session, and searching all goals is hard.
            // But wait, the original code `query.eq("status", "RUNNING")` would find ANY running session in the DB!
            // That was a bug if multiple users existed.
            // Now we MUST fix it.
            // We need to find a running session belonging to a goal owned by userId.
            // This requires a JOIN or subquery.
            // Since we use MyBatis Plus, we can select goals by user, then search sessions in those goals.
            
            List<GoalWithStatsDto> userGoals = goalService.getGoals(userId, null);
            List<Long> goalIds = userGoals.stream().map(GoalWithStatsDto::getId).collect(Collectors.toList());
            
            if (goalIds.isEmpty()) {
                 throw new RuntimeException("No running focus session found");
            }

            QueryWrapper<FocusSession> query = new QueryWrapper<>();
            query.eq("status", "RUNNING");
            query.in("goal_id", goalIds);
            session = focusSessionMapper.selectOne(query);
            
            if (session == null) {
                throw new RuntimeException("No running focus session found");
            }
        }
        
        // Verify ownership
        Goal goal = goalService.getGoalById(session.getGoalId());
        if (goal == null || !goal.getUserId().equals(userId)) {
             throw new RuntimeException("Access denied for this session");
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

    public FocusStatsDto getStatistics(Long userId, Long goalId) {
        userService.getInternalUserId(userId);
        if (goalId == null) {
            throw new IllegalArgumentException("Goal ID is required");
        }

        Goal goal = goalService.getGoalById(goalId);
        if (goal == null || !goal.getUserId().equals(userId)) {
            throw new RuntimeException("Goal not found or access denied");
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
                .filter(s -> s.getStartTime() != null && s.getStartTime().toLocalDate().isAfter(sevenDaysAgo))
                .mapToLong(s -> s.getDurationMinutes() != null ? s.getDurationMinutes() : 0)
                .sum();

        long last30DaysMinutes = sessions.stream()
                .filter(s -> s.getStartTime() != null && s.getStartTime().toLocalDate().isAfter(thirtyDaysAgo))
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

    public FocusSession getRunningSession(Long userId, Long goalId) {
        userService.getInternalUserId(userId);
        QueryWrapper<FocusSession> query = new QueryWrapper<>();
        query.eq("status", "RUNNING");
        
        if (goalId != null) {
            Goal goal = goalService.getGoalById(goalId);
            if (goal == null || !goal.getUserId().equals(userId)) {
                throw new RuntimeException("Goal not found or access denied");
            }
            query.eq("goal_id", goalId);
        } else {
            List<GoalWithStatsDto> userGoals = goalService.getGoals(userId, null);
            List<Long> goalIds = userGoals.stream().map(GoalWithStatsDto::getId).collect(Collectors.toList());
            if (goalIds.isEmpty()) {
                return null;
            }
            query.in("goal_id", goalIds);
        }
        
        return focusSessionMapper.selectOne(query);
    }

    public FocusSessionDto getFocusSessionById(Long userId, Long id) {
        userService.getInternalUserId(userId);
        FocusSession session = focusSessionMapper.selectById(id);
        if (session == null) {
            throw new RuntimeException("Focus session not found with id: " + id);
        }

        Goal goal = goalService.getGoalById(session.getGoalId());
        if (goal == null || !goal.getUserId().equals(userId)) {
             throw new RuntimeException("Access denied");
        }

        FocusSessionDto dto = new FocusSessionDto();
        BeanUtils.copyProperties(session, dto);
        dto.setGoalTitle(goal.getTitle());

        return dto;
    }

    public List<FocusSessionDto> getFocusSessionList(Long userId, Long goalId) {
        userService.getInternalUserId(userId);
        QueryWrapper<FocusSession> query = new QueryWrapper<>();
        
        if (goalId != null) {
            Goal goal = goalService.getGoalById(goalId);
            if (goal == null || !goal.getUserId().equals(userId)) {
                 throw new RuntimeException("Goal not found or access denied");
            }
            query.eq("goal_id", goalId);
        } else {
             List<GoalWithStatsDto> userGoals = goalService.getGoals(userId, null);
             List<Long> goalIds = userGoals.stream().map(GoalWithStatsDto::getId).collect(Collectors.toList());
             if (goalIds.isEmpty()) {
                 return new ArrayList<>();
             }
             query.in("goal_id", goalIds);
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
