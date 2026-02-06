package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.entity.Goal;
import com.life.yolo.mapper.GoalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoalService {

    @Autowired
    private GoalMapper goalMapper;

    public Goal createGoal(String title, String description) {
        Goal goal = new Goal();
        goal.setTitle(title);
        goal.setDescription(description);
        goal.setStatus("ACTIVE");
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        
        goalMapper.insert(goal);
        return goal;
    }

    public List<Goal> getGoals() {
        QueryWrapper<Goal> query = new QueryWrapper<>();
        query.orderByDesc("created_at");
        return goalMapper.selectList(query);
    }

    public Goal getGoalById(Long goalId) {
        return goalMapper.selectById(goalId);
    }
}
