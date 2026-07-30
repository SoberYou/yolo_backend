package com.life.yolo.controller;

import com.life.yolo.common.ApiResponse;
import com.life.yolo.dto.SopSortUpdateDto;
import com.life.yolo.dto.SopTemplateDetailDto;
import com.life.yolo.entity.SopTemplate;
import com.life.yolo.service.SopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sop")
@RequiredArgsConstructor
public class SopController {

    private final SopService sopService;

    // --- Template Endpoints ---

    @PostMapping("/template/create")
    public ApiResponse<Long> createTemplate(@RequestParam Long userId, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        return ApiResponse.success(sopService.createTemplate(userId, name));
    }

    @GetMapping("/template/list")
    public ApiResponse<List<SopTemplate>> listTemplates(@RequestParam Long userId) {
        return ApiResponse.success(sopService.listTemplates(userId));
    }

    @GetMapping("/template/detail/{templateId}")
    public ApiResponse<SopTemplateDetailDto> getTemplateDetail(@RequestParam Long userId, @PathVariable Long templateId) {
        return ApiResponse.success(sopService.getTemplateDetail(userId, templateId));
    }

    @PutMapping("/template/update/{templateId}")
    public ApiResponse<Void> updateTemplate(@RequestParam Long userId, @PathVariable Long templateId, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        sopService.updateTemplate(userId, templateId, name);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/template/delete/{templateId}")
    public ApiResponse<Void> deleteTemplate(@RequestParam Long userId, @PathVariable Long templateId) {
        sopService.deleteTemplate(userId, templateId);
        return ApiResponse.success(null);
    }

    // --- Category Endpoints ---

    @PostMapping("/category/create")
    public ApiResponse<Long> createCategory(@RequestParam Long userId, @RequestBody Map<String, Object> body) {
        Long templateId = Long.valueOf(body.get("templateId").toString());
        String name = (String) body.get("name");
        String type = (String) body.get("type");
        return ApiResponse.success(sopService.createCategory(userId, templateId, name, type));
    }

    @PutMapping("/category/update/{categoryId}")
    public ApiResponse<Void> updateCategory(@RequestParam Long userId, @PathVariable Long categoryId, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        sopService.updateCategory(userId, categoryId, name);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/category/delete/{categoryId}")
    public ApiResponse<Void> deleteCategory(@RequestParam Long userId, @PathVariable Long categoryId) {
        sopService.deleteCategory(userId, categoryId);
        return ApiResponse.success(null);
    }

    @PostMapping("/category/batchUpdateSort")
    public ApiResponse<Void> batchUpdateCategorySort(@RequestParam Long userId, @RequestBody List<SopSortUpdateDto> sortUpdates) {
        sopService.batchUpdateCategorySort(userId, sortUpdates);
        return ApiResponse.success(null);
    }

    // --- Item Endpoints ---

    @PostMapping("/item/create")
    public ApiResponse<Long> createItem(@RequestParam Long userId, @RequestBody Map<String, Object> body) {
        Long categoryId = Long.valueOf(body.get("categoryId").toString());
        String itemKey = (String) body.get("itemKey");
        String itemValue = (String) body.get("itemValue");
        return ApiResponse.success(sopService.createItem(userId, categoryId, itemKey, itemValue));
    }

    @PutMapping("/item/update/{itemId}")
    public ApiResponse<Void> updateItem(@RequestParam Long userId, @PathVariable Long itemId, @RequestBody Map<String, String> body) {
        String itemKey = body.get("itemKey");
        String itemValue = body.get("itemValue");
        sopService.updateItem(userId, itemId, itemKey, itemValue);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/item/delete/{itemId}")
    public ApiResponse<Void> deleteItem(@RequestParam Long userId, @PathVariable Long itemId) {
        sopService.deleteItem(userId, itemId);
        return ApiResponse.success(null);
    }

    @PostMapping("/item/batchUpdateSort")
    public ApiResponse<Void> batchUpdateItemSort(@RequestParam Long userId, @RequestBody List<SopSortUpdateDto> sortUpdates) {
        sopService.batchUpdateItemSort(userId, sortUpdates);
        return ApiResponse.success(null);
    }
}
