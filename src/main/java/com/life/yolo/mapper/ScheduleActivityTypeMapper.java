package com.life.yolo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.life.yolo.dto.ActivityTypeSortUpdateDto;
import com.life.yolo.entity.ScheduleActivityType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScheduleActivityTypeMapper extends BaseMapper<ScheduleActivityType> {
    int batchUpdateSortOrder(@Param("list") List<ActivityTypeSortUpdateDto> list, @Param("userId") Long userId);
}
