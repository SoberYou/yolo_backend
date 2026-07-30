package com.life.yolo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.life.yolo.dto.SopSortUpdateDto;
import com.life.yolo.entity.SopItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SopItemMapper extends BaseMapper<SopItem> {
    int batchUpdateSortOrder(@Param("list") List<SopSortUpdateDto> list, @Param("userId") Long userId);
}
