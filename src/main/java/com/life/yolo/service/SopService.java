package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.life.yolo.dto.SopCategoryDto;
import com.life.yolo.dto.SopSortUpdateDto;
import com.life.yolo.dto.SopTemplateDetailDto;
import com.life.yolo.entity.SopCategory;
import com.life.yolo.entity.SopItem;
import com.life.yolo.entity.SopTemplate;
import com.life.yolo.mapper.SopCategoryMapper;
import com.life.yolo.mapper.SopItemMapper;
import com.life.yolo.mapper.SopTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SopService {

    private final SopTemplateMapper templateMapper;
    private final SopCategoryMapper categoryMapper;
    private final SopItemMapper itemMapper;

    // --- Template Operations ---

    @Transactional(rollbackFor = Exception.class)
    public Long createTemplate(Long userId, String name) {
        SopTemplate template = new SopTemplate();
        template.setUserId(userId);
        template.setName(name);
        templateMapper.insert(template);
        return template.getId();
    }

    public List<SopTemplate> listTemplates(Long userId) {
        return templateMapper.selectList(new LambdaQueryWrapper<SopTemplate>()
                .eq(SopTemplate::getUserId, userId)
                .orderByDesc(SopTemplate::getUpdateTime));
    }

    public SopTemplateDetailDto getTemplateDetail(Long userId, Long templateId) {
        SopTemplate template = templateMapper.selectOne(new LambdaQueryWrapper<SopTemplate>()
                .eq(SopTemplate::getId, templateId)
                .eq(SopTemplate::getUserId, userId));
        if (template == null) {
            return null;
        }

        SopTemplateDetailDto detailDto = new SopTemplateDetailDto();
        detailDto.setId(template.getId());
        detailDto.setUserId(template.getUserId());
        detailDto.setName(template.getName());
        detailDto.setCreateTime(template.getCreateTime());
        detailDto.setUpdateTime(template.getUpdateTime());

        List<SopCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<SopCategory>()
                .eq(SopCategory::getTemplateId, templateId)
                .eq(SopCategory::getUserId, userId)
                .orderByAsc(SopCategory::getSortOrder));

        if (categories.isEmpty()) {
            detailDto.setCategories(new ArrayList<>());
            return detailDto;
        }

        List<Long> categoryIds = categories.stream().map(SopCategory::getId).collect(Collectors.toList());
        List<SopItem> allItems = itemMapper.selectList(new LambdaQueryWrapper<SopItem>()
                .in(SopItem::getCategoryId, categoryIds)
                .eq(SopItem::getUserId, userId)
                .orderByAsc(SopItem::getSortOrder));

        Map<Long, List<SopItem>> itemsByCategory = allItems.stream()
                .collect(Collectors.groupingBy(SopItem::getCategoryId));

        List<SopCategoryDto> categoryDtos = categories.stream().map(category -> {
            SopCategoryDto dto = new SopCategoryDto();
            dto.setId(category.getId());
            dto.setTemplateId(category.getTemplateId());
            dto.setUserId(category.getUserId());
            dto.setName(category.getName());
            dto.setType(category.getType());
            dto.setSortOrder(category.getSortOrder());
            dto.setCreateTime(category.getCreateTime());
            dto.setUpdateTime(category.getUpdateTime());
            dto.setItems(itemsByCategory.getOrDefault(category.getId(), new ArrayList<>()));
            return dto;
        }).collect(Collectors.toList());

        detailDto.setCategories(categoryDtos);
        return detailDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(Long userId, Long templateId, String name) {
        SopTemplate template = new SopTemplate();
        template.setId(templateId);
        template.setName(name);
        templateMapper.update(template, new LambdaQueryWrapper<SopTemplate>()
                .eq(SopTemplate::getId, templateId)
                .eq(SopTemplate::getUserId, userId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long userId, Long templateId) {
        templateMapper.delete(new LambdaQueryWrapper<SopTemplate>()
                .eq(SopTemplate::getId, templateId)
                .eq(SopTemplate::getUserId, userId));

        List<SopCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<SopCategory>()
                .eq(SopCategory::getTemplateId, templateId)
                .eq(SopCategory::getUserId, userId));
        
        if (!categories.isEmpty()) {
            List<Long> categoryIds = categories.stream().map(SopCategory::getId).collect(Collectors.toList());
            itemMapper.delete(new LambdaQueryWrapper<SopItem>()
                    .in(SopItem::getCategoryId, categoryIds)
                    .eq(SopItem::getUserId, userId));
            
            categoryMapper.delete(new LambdaQueryWrapper<SopCategory>()
                    .eq(SopCategory::getTemplateId, templateId)
                    .eq(SopCategory::getUserId, userId));
        }
    }

    // --- Category Operations ---

    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(Long userId, Long templateId, String name, String type) {
        SopCategory category = new SopCategory();
        category.setUserId(userId);
        category.setTemplateId(templateId);
        category.setName(name);
        category.setType(type);
        category.setSortOrder(0); // Can be enhanced to fetch max sort
        categoryMapper.insert(category);
        return category.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long userId, Long categoryId, String name) {
        SopCategory category = new SopCategory();
        category.setId(categoryId);
        category.setName(name);
        categoryMapper.update(category, new LambdaQueryWrapper<SopCategory>()
                .eq(SopCategory::getId, categoryId)
                .eq(SopCategory::getUserId, userId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long userId, Long categoryId) {
        categoryMapper.delete(new LambdaQueryWrapper<SopCategory>()
                .eq(SopCategory::getId, categoryId)
                .eq(SopCategory::getUserId, userId));
        
        itemMapper.delete(new LambdaQueryWrapper<SopItem>()
                .eq(SopItem::getCategoryId, categoryId)
                .eq(SopItem::getUserId, userId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateCategorySort(Long userId, List<SopSortUpdateDto> sortUpdates) {
        if (sortUpdates != null && !sortUpdates.isEmpty()) {
            categoryMapper.batchUpdateSortOrder(sortUpdates, userId);
        }
    }

    // --- Item Operations ---

    @Transactional(rollbackFor = Exception.class)
    public Long createItem(Long userId, Long categoryId, String itemKey, String itemValue) {
        SopItem item = new SopItem();
        item.setUserId(userId);
        item.setCategoryId(categoryId);
        item.setItemKey(itemKey);
        item.setItemValue(itemValue);
        item.setSortOrder(0);
        itemMapper.insert(item);
        return item.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateItem(Long userId, Long itemId, String itemKey, String itemValue) {
        SopItem item = new SopItem();
        item.setId(itemId);
        item.setItemKey(itemKey);
        item.setItemValue(itemValue);
        itemMapper.update(item, new LambdaQueryWrapper<SopItem>()
                .eq(SopItem::getId, itemId)
                .eq(SopItem::getUserId, userId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long userId, Long itemId) {
        itemMapper.delete(new LambdaQueryWrapper<SopItem>()
                .eq(SopItem::getId, itemId)
                .eq(SopItem::getUserId, userId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateItemSort(Long userId, List<SopSortUpdateDto> sortUpdates) {
        if (sortUpdates != null && !sortUpdates.isEmpty()) {
            itemMapper.batchUpdateSortOrder(sortUpdates, userId);
        }
    }
}
