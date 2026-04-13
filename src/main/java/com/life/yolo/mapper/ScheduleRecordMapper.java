package com.life.yolo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.life.yolo.entity.ScheduleRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScheduleRecordMapper extends BaseMapper<ScheduleRecord> {
    
    /**
     * 批量保存日程记录，如果存在相同 biz_date, time_slot, record_type 的记录则更新
     */
    int batchSaveRecords(@Param("list") List<ScheduleRecord> records);

    /**
     * 通过 userId 和 goalId 查询关联的日程记录
     */
    List<ScheduleRecord> selectRecordsByGoalId(@Param("userId") Long userId, @Param("goalId") Long goalId);
}
