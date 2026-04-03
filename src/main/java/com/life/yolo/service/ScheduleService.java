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

    public List<ScheduleActivityType> getActivityTypes(Long userId, String typeName) {
        QueryWrapper<ScheduleActivityType> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        if (StringUtils.hasText(typeName)) {
            query.like("type_name", typeName);
        }
        query.orderByAsc("sort").orderByDesc("create_time");
        return activityTypeMapper.selectList(query);
    }

    public ScheduleActivityType createActivityType(ScheduleActivityType type, Long userId) {
        type.setUserId(userId);
        type.setCreateTime(LocalDateTime.now());
        type.setUpdateTime(LocalDateTime.now());
        activityTypeMapper.insert(type);
        return type;
    }

    public ScheduleActivityType updateActivityType(ScheduleActivityType type, Long userId) {
        QueryWrapper<ScheduleActivityType> query = new QueryWrapper<>();
        query.eq("id", type.getId()).eq("user_id", userId);
        type.setUpdateTime(LocalDateTime.now());
        activityTypeMapper.update(type, query);
        return type;
    }

    public void deleteActivityType(Long id, Long userId) {
        QueryWrapper<ScheduleActivityType> query = new QueryWrapper<>();
        query.eq("id", id).eq("user_id", userId);
        activityTypeMapper.delete(query);
    }

    // --- ScheduleRecord ---

    public List<ScheduleRecord> getRecords(Long userId, LocalDate startDate, LocalDate endDate, String recordType) {
        QueryWrapper<ScheduleRecord> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        if (startDate != null) {
            query.ge("biz_date", startDate);
        }
        if (endDate != null) {
            query.le("biz_date", endDate);
        }
        if (StringUtils.hasText(recordType)) {
            query.eq("record_type", recordType);
        }
        query.orderByAsc("biz_date").orderByAsc("start_time");
        return recordMapper.selectList(query);
    }

    public void deleteRecord(Long id, Long userId) {
        QueryWrapper<ScheduleRecord> query = new QueryWrapper<>();
        query.eq("id", id).eq("user_id", userId);
        recordMapper.delete(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSaveRecords(Long userId, List<ScheduleRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        // Validate and set userId for records
        for (ScheduleRecord record : records) {
            if (record.getBizDate() == null || record.getStartTime() == null || record.getEndTime() == null) {
                throw new IllegalArgumentException("bizDate, startTime and endTime are required for saving schedule record");
            }
            if (!StringUtils.hasText(record.getRecordType())) {
                record.setRecordType(""); // default to empty string if null or empty to satisfy DB constraints if needed
            }
            record.setUserId(userId);
        }

        // Use mapper's ON DUPLICATE KEY UPDATE method
        recordMapper.batchSaveRecords(records);

        // Delete records where activity_type is empty to avoid dirty data
        List<LocalDate> bizDates = records.stream().map(ScheduleRecord::getBizDate).distinct().collect(Collectors.toList());
        List<String> recordTypes = records.stream().map(ScheduleRecord::getRecordType).distinct().collect(Collectors.toList());

        QueryWrapper<ScheduleRecord> deleteQuery = new QueryWrapper<>();
        deleteQuery.eq("user_id", userId)
                .in("biz_date", bizDates)
                .in("record_type", recordTypes)
                .and(w -> w.isNull("activity_type").or().eq("activity_type", ""));
        
        recordMapper.delete(deleteQuery);
    }
}
