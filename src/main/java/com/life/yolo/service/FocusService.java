package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.entity.FocusSession;
import com.life.yolo.mapper.FocusSessionMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class FocusService {

    @Autowired
    private FocusSessionMapper focusSessionMapper;

    @Transactional(rollbackFor = Exception.class)
    public FocusSession startFocus(Long goalId) {
        // Check if there is any running session
        QueryWrapper<FocusSession> query = new QueryWrapper<>();
        query.eq("status", "RUNNING");
        Long count = focusSessionMapper.selectCount(query);
        
        if (count > 0) {
            throw new RuntimeException("There is already a running focus session");
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
    public FocusSession endFocus() {
        QueryWrapper<FocusSession> query = new QueryWrapper<>();
        query.eq("status", "RUNNING");
        FocusSession session = focusSessionMapper.selectOne(query);
        
        if (session == null) {
            throw new RuntimeException("No running focus session found");
        }

        LocalDateTime now = LocalDateTime.now();
        session.setEndTime(now);
        session.setStatus("COMPLETED");
        
        long minutes = ChronoUnit.MINUTES.between(session.getStartTime(), now);
        session.setDurationMinutes((int) minutes);
        session.setUpdatedAt(now);
        
        focusSessionMapper.updateById(session);
        return session;
    }

    public FocusStatsDto getStatistics(Long goalId) {
        QueryWrapper<FocusSession> query = new QueryWrapper<>();
        if (goalId != null) {
            query.eq("goal_id", goalId);
        }
        query.eq("status", "COMPLETED");
        
        List<FocusSession> sessions = focusSessionMapper.selectList(query);
        
        long totalMinutes = 0;
        int totalSessions = sessions.size();
        
        for (FocusSession session : sessions) {
            if (session.getDurationMinutes() != null) {
                totalMinutes += session.getDurationMinutes();
            }
        }
        
        FocusStatsDto dto = new FocusStatsDto();
        dto.setGoalId(goalId);
        dto.setTotalSessions(totalSessions);
        dto.setTotalMinutes(totalMinutes);
        
        return dto;
    }

    @Data
    public static class FocusStatsDto {
        private Long goalId;
        private Integer totalSessions;
        private Long totalMinutes;
    }
}
