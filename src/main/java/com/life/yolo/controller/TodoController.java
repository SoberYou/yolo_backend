package com.life.yolo.controller;

import com.life.yolo.common.ApiResponse;
import com.life.yolo.dto.TodoSortUpdateDto;
import com.life.yolo.entity.TodoItem;
import com.life.yolo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping("/getTodos")
    public ApiResponse<List<TodoItem>> getTodos(
            @RequestParam Long userId,
            @RequestParam(required = false) String dateType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer isCompleted) {
        
        List<TodoItem> todos = todoService.getTodos(userId, dateType, startDate, endDate, isCompleted);
        return ApiResponse.success(todos);
    }

    @PostMapping("/createTodo")
    public ApiResponse<TodoItem> createTodo(@RequestBody TodoItem todoItem, @RequestParam Long userId) {
        TodoItem created = todoService.createTodo(todoItem, userId);
        return ApiResponse.success(created);
    }

    @PutMapping("/updateTodo/{id}")
    public ApiResponse<TodoItem> updateTodo(
            @PathVariable Long id, 
            @RequestBody TodoItem todoItem, 
            @RequestParam Long userId) {
        TodoItem updated = todoService.updateTodo(id, todoItem, userId);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/deleteTodo/{id}")
    public ApiResponse<Void> deleteTodo(@PathVariable Long id, @RequestParam Long userId) {
        todoService.deleteTodo(id, userId);
        return ApiResponse.success(null);
    }

    @PutMapping("/batchUpdateSort")
    public ApiResponse<Void> batchUpdateSort(
            @RequestBody List<TodoSortUpdateDto> sortUpdates, 
            @RequestParam Long userId) {
        todoService.batchUpdateSortOrder(sortUpdates, userId);
        return ApiResponse.success(null);
    }
}
