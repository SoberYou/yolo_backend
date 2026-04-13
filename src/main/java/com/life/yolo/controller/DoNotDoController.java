package com.life.yolo.controller;

import com.life.yolo.common.ApiResponse;
import com.life.yolo.dto.DoNotDoSortUpdateDto;
import com.life.yolo.entity.DoNotDoItem;
import com.life.yolo.service.DoNotDoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donotdo")
@RequiredArgsConstructor
public class DoNotDoController {

    private final DoNotDoService doNotDoService;

    @GetMapping("/getItems")
    public ApiResponse<List<DoNotDoItem>> getItems(
            @RequestParam Long userId,
            @RequestParam(required = false) String itemType) {
        
        List<DoNotDoItem> items = doNotDoService.getItems(userId, itemType);
        return ApiResponse.success(items);
    }

    @PostMapping("/createItem")
    public ApiResponse<DoNotDoItem> createItem(@RequestBody DoNotDoItem item, @RequestParam Long userId) {
        DoNotDoItem created = doNotDoService.createItem(item, userId);
        return ApiResponse.success(created);
    }

    @PutMapping("/updateItem/{id}")
    public ApiResponse<DoNotDoItem> updateItem(
            @PathVariable Long id, 
            @RequestBody DoNotDoItem item, 
            @RequestParam Long userId) {
        DoNotDoItem updated = doNotDoService.updateItem(id, item, userId);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/deleteItem/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id, @RequestParam Long userId) {
        doNotDoService.deleteItem(id, userId);
        return ApiResponse.success(null);
    }

    @PutMapping("/batchUpdateSort")
    public ApiResponse<Void> batchUpdateSort(
            @RequestBody List<DoNotDoSortUpdateDto> sortUpdates, 
            @RequestParam Long userId) {
        doNotDoService.batchUpdateSortOrder(sortUpdates, userId);
        return ApiResponse.success(null);
    }
}
