package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.dto.DoNotDoSortUpdateDto;
import com.life.yolo.entity.DoNotDoItem;
import com.life.yolo.mapper.DoNotDoItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoNotDoService {

    private final DoNotDoItemMapper doNotDoItemMapper;

    public List<DoNotDoItem> getItems(Long userId, String itemType) {
        QueryWrapper<DoNotDoItem> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        
        if (StringUtils.hasText(itemType)) {
            query.eq("item_type", itemType);
        }
        
        query.orderByAsc("sort_order").orderByDesc("create_time");
        return doNotDoItemMapper.selectList(query);
    }

    public DoNotDoItem createItem(DoNotDoItem item, Long userId) {
        item.setUserId(userId);
        if (item.getSortOrder() == null) {
            item.setSortOrder(0);
        }
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        doNotDoItemMapper.insert(item);
        return item;
    }

    public DoNotDoItem updateItem(Long id, DoNotDoItem item, Long userId) {
        DoNotDoItem existing = doNotDoItemMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Do Not Do item not found or unauthorized");
        }

        if (StringUtils.hasText(item.getItemType())) existing.setItemType(item.getItemType());
        if (StringUtils.hasText(item.getContent())) existing.setContent(item.getContent());
        if (item.getSortOrder() != null) existing.setSortOrder(item.getSortOrder());

        existing.setUpdateTime(LocalDateTime.now());
        doNotDoItemMapper.updateById(existing);
        return existing;
    }

    public void deleteItem(Long id, Long userId) {
        DoNotDoItem existing = doNotDoItemMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Do Not Do item not found or unauthorized");
        }
        doNotDoItemMapper.deleteById(id);
    }

    @Transactional
    public void batchUpdateSortOrder(List<DoNotDoSortUpdateDto> sortUpdates, Long userId) {
        if (sortUpdates == null || sortUpdates.isEmpty()) {
            return;
        }
        doNotDoItemMapper.batchUpdateSortOrder(sortUpdates, userId);
    }
}
