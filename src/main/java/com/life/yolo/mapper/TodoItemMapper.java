package com.life.yolo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.life.yolo.dto.TodoSortUpdateDto;
import com.life.yolo.entity.TodoItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TodoItemMapper extends BaseMapper<TodoItem> {

    int batchUpdateSortOrder(@Param("list") List<TodoSortUpdateDto> list, @Param("userId") Long userId);
}
