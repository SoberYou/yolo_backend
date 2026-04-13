package com.life.yolo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.life.yolo.dto.DoNotDoSortUpdateDto;
import com.life.yolo.entity.DoNotDoItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DoNotDoItemMapper extends BaseMapper<DoNotDoItem> {

    int batchUpdateSortOrder(@Param("list") List<DoNotDoSortUpdateDto> list, @Param("userId") Long userId);
}
