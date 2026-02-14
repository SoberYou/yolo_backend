package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.entity.LifeProfile;
import com.life.yolo.entity.User;
import com.life.yolo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public Long getInternalUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }

        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        User user = userMapper.selectOne(query);

        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        return user.getId();
    }
}
