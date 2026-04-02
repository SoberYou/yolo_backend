package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.life.yolo.entity.User;
import com.life.yolo.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class AuthService {

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    @Autowired
    private UserMapper userMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public User login(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code
        );

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();
            log.info("WeChat login response: {}", body);

            JsonNode root = objectMapper.readTree(body);
            if (root.has("errcode") && root.get("errcode").asInt() != 0) {
                String errmsg = root.has("errmsg") ? root.get("errmsg").asText() : "Unknown error";
                throw new RuntimeException("WeChat login failed: " + errmsg);
            }

            String openid = root.get("openid").asText();
            String sessionKey = root.get("session_key").asText();

            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("openid", openid);
            User user = userMapper.selectOne(queryWrapper);

            if (user == null) {
                user = new User();
                user.setUserId(generateUniqueUserId());
                user.setOpenid(openid);
                user.setSessionKey(sessionKey);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.insert(user);
            } else {
                // Ensure userId is preserved (already handled by retrieving existing user)
                user.setSessionKey(sessionKey);
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(user);
            }

            return user;

        } catch (Exception e) {
            log.error("WeChat login error", e);
            throw new RuntimeException("WeChat login failed", e);
        }
    }

    private Long generateUniqueUserId() {
        while (true) {
            // Generate random number up to 8 digits (1 to 99999999)
            long userId = ThreadLocalRandom.current().nextLong(1, 100000000);
            
            QueryWrapper<User> query = new QueryWrapper<>();
            query.eq("user_id", userId);
            if (userMapper.selectCount(query) == 0) {
                return userId;
            }
        }
    }
}
