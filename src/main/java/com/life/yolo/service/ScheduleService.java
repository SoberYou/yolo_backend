package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.entity.ScheduleActivityType;
import com.life.yolo.entity.ScheduleRecord;
import com.life.yolo.mapper.ScheduleActivityTypeMapper;
import com.life.yolo.mapper.ScheduleRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleActivityTypeMapper activityTypeMapper;
    private final ScheduleRecordMapper recordMapper;

    // --- ScheduleActivityType ---

    public List<ScheduleActivityType> getActivityTypes(String typeName) {
        QueryWrapper<ScheduleActivityType> query = new QueryWrapper<>();
        if (StringUtils.hasText(typeName)) {
            query.like("type_name", typeName);
        }
        query.orderByAsc("sort").orderByDesc("create_time");
        return activityTypeMapper.selectList(query);
    }

    public ScheduleActivityType createActivityType(ScheduleActivityType type) {
        type.setCreateTime(LocalDateTime.now());
        type.setUpdateTime(LocalDateTime.now());
        activityTypeMapper.insert(type);
        return type;
    }

    public ScheduleActivityType updateActivityType(ScheduleActivityType type) {
        type.setUpdateTime(LocalDateTime.now());
        activityTypeMapper.updateById(type);
        return type;
    }

    public void deleteActivityType(Long id) {
        activityTypeMapper.deleteById(id);
    }

    // --- ScheduleRecord ---

    public List<ScheduleRecord> getRecords(LocalDate bizDate, String recordType) {
        QueryWrapper<ScheduleRecord> query = new QueryWrapper<>();
        if (bizDate != null) {
            query.eq("biz_date", bizDate);
        }
        if (StringUtils.hasText(recordType)) {
            query.eq("record_type", recordType);
        }
        query.orderByAsc("time_slot");
        return recordMapper.selectList(query);
    }

    public void deleteRecord(Long id) {
        recordMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSaveRecords(List<ScheduleRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        // Validate records
        for (ScheduleRecord record : records) {
            if (record.getBizDate() == null || record.getTimeSlot() == null) {
                throw new IllegalArgumentException("bizDate and timeSlot are required for saving schedule record");
            }
            if (!StringUtils.hasText(record.getRecordType())) {
                record.setRecordType(""); // default to empty string if null or empty to satisfy DB constraints if needed
            }
        }

        // Use mapper's ON DUPLICATE KEY UPDATE method
        recordMapper.batchSaveRecords(records);
    }
}
