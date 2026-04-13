package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.dto.DailyFocusRecord;
import com.life.yolo.dto.FocusSessionDto;
import com.life.yolo.dto.FocusStatsDto;
import com.life.yolo.dto.GoalWithStatsDto;
import com.life.yolo.entity.FocusSession;
import com.life.yolo.entity.Goal;
import com.life.yolo.entity.GoalActivityTypeRelation;
import com.life.yolo.entity.ScheduleRecord;
import com.life.yolo.mapper.FocusSessionMapper;
import com.life.yolo.mapper.GoalActivityTypeRelationMapper;
import com.life.yolo.mapper.ScheduleRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FocusService {

    private final FocusSessionMapper focusSessionMapper;
    private final GoalService goalService;
    private final UserService userService;
    private final GoalActivityTypeRelationMapper goalActivityTypeRelationMapper;
    private final ScheduleRecordMapper scheduleRecordMapper;

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

        // 1. 使用 mapper.xml 中的 SQL 关联查询
        List<ScheduleRecord> relatedRecords = scheduleRecordMapper.selectRecordsByGoalId(userId, goalId);

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        LocalDate thirtyDaysAgo = today.minusDays(30);

        long totalMinutes = 0;
        long last7DaysMinutes = 0;
        long last30DaysMinutes = 0;
        Map<LocalDate, Integer> dailyMap = new java.util.HashMap<>();

        // 2. 计算各个日程记录的投入时间
        for (ScheduleRecord record : relatedRecords) {
            if (record.getStartTime() == null || record.getEndTime() == null || record.getBizDate() == null) {
                continue;
            }
            try {
                LocalTime startTime = LocalTime.parse(record.getStartTime());
                LocalTime endTime = LocalTime.parse(record.getEndTime());
                LocalDate bizDate = record.getBizDate();
                
                // 计算分钟差
                long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
                // 跨天的情况简单处理
                if (minutes < 0) {
                    minutes += 24 * 60;
                }

                totalMinutes += minutes;

                if (bizDate.isAfter(sevenDaysAgo) && !bizDate.isAfter(today)) {
                    last7DaysMinutes += minutes;
                }

                if (bizDate.isAfter(thirtyDaysAgo) && !bizDate.isAfter(today)) {
                    last30DaysMinutes += minutes;
                }

                dailyMap.merge(bizDate, (int) minutes, Integer::sum);
            } catch (Exception e) {
                // 忽略解析错误的记录
                continue;
            }
        }

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
