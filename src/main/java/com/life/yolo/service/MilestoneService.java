package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.entity.Milestone;
import com.life.yolo.mapper.MilestoneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MilestoneService {

    @Autowired
    private MilestoneMapper milestoneMapper;

    @Transactional(rollbackFor = Exception.class)
    public Milestone saveMilestone(Milestone milestone) {
        if (milestone.getId() == null) {
            milestone.setCreateAt(LocalDateTime.now());
            milestone.setUpdateAt(LocalDateTime.now());
            milestoneMapper.insert(milestone);
        } else {
            milestone.setUpdateAt(LocalDateTime.now());
            // Ensure createAt is not null or overwritten if needed, but usually update doesn't touch createAt unless specified
            // Since we are using an object from controller, createAt might be null.
            // We should fetch existing if we want to preserve createAt, or assume the client sends it, 
            // or just let MP handle partial updates if we use updateById (MP defaults to not updating nulls usually depending on config).
            // But let's be safe and just update.
            // Actually, best practice for update is to fetch first or use dynamic update.
            // Let's assume this is a partial update or full update.
            // If I just call updateById, MP by default ignores null fields.
            milestoneMapper.updateById(milestone);
            // Fetch back to return full object
            return milestoneMapper.selectById(milestone.getId());
        }
        return milestone;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMilestone(Integer id) {
        milestoneMapper.deleteById(id);
    }

    public List<Milestone> getMilestoneList(Long goalId) {
        QueryWrapper<Milestone> query = new QueryWrapper<>();
        if (goalId != null) {
            query.eq("goal_id", goalId);
        }
        query.orderByDesc("milestone_date");
        return milestoneMapper.selectList(query);
    }

    public Milestone getMilestoneById(Integer id) {
        return milestoneMapper.selectById(id);
    }
}
