package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.dto.TodoSortUpdateDto;
import com.life.yolo.entity.TodoItem;
import com.life.yolo.mapper.TodoItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoItemMapper todoItemMapper;

    public List<TodoItem> getTodos(Long userId, String dateType, String startDate, String endDate, Integer isCompleted) {
        QueryWrapper<TodoItem> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        
        if (StringUtils.hasText(dateType)) {
            query.eq("date_type", dateType);
        }
        if (StringUtils.hasText(startDate)) {
            query.ge("start_date", startDate);
        }
        if (StringUtils.hasText(endDate)) {
            query.le("end_date", endDate);
        }
        if (isCompleted != null) {
            query.eq("is_completed", isCompleted);
        }
        
        query.orderByAsc("is_completed").orderByAsc("sort_order").orderByAsc("start_date").orderByDesc("create_time");
        return todoItemMapper.selectList(query);
    }

    public TodoItem createTodo(TodoItem todoItem, Long userId) {
        todoItem.setUserId(userId);
        if (todoItem.getIsCompleted() == null) {
            todoItem.setIsCompleted(0);
        }
        if (todoItem.getSortOrder() == null) {
            todoItem.setSortOrder(0);
        }
        if (!StringUtils.hasText(todoItem.getPriority())) {
            todoItem.setPriority("MEDIUM");
        }
        todoItem.setCreateTime(LocalDateTime.now());
        todoItem.setUpdateTime(LocalDateTime.now());
        todoItemMapper.insert(todoItem);
        return todoItem;
    }

    public TodoItem updateTodo(Long id, TodoItem todoItem, Long userId) {
        TodoItem existing = todoItemMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Todo item not found or unauthorized");
        }

        if (StringUtils.hasText(todoItem.getDateType())) existing.setDateType(todoItem.getDateType());
        if (StringUtils.hasText(todoItem.getStartDate())) existing.setStartDate(todoItem.getStartDate());
        if (StringUtils.hasText(todoItem.getEndDate())) existing.setEndDate(todoItem.getEndDate());
        if (StringUtils.hasText(todoItem.getContent())) existing.setContent(todoItem.getContent());
        if (StringUtils.hasText(todoItem.getPriority())) existing.setPriority(todoItem.getPriority());
        if (todoItem.getSortOrder() != null) existing.setSortOrder(todoItem.getSortOrder());
        if (todoItem.getIsCompleted() != null) existing.setIsCompleted(todoItem.getIsCompleted());

        existing.setUpdateTime(LocalDateTime.now());
        todoItemMapper.updateById(existing);
        return existing;
    }

    public void deleteTodo(Long id, Long userId) {
        TodoItem existing = todoItemMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Todo item not found or unauthorized");
        }
        todoItemMapper.deleteById(id);
    }

    @Transactional
    public void batchUpdateSortOrder(List<TodoSortUpdateDto> sortUpdates, Long userId) {
        if (sortUpdates == null || sortUpdates.isEmpty()) {
            return;
        }
        todoItemMapper.batchUpdateSortOrder(sortUpdates, userId);
    }
}
