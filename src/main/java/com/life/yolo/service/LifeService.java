package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.entity.LifeProfile;
import com.life.yolo.mapper.LifeProfileMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class LifeService {

    @Autowired
    private LifeProfileMapper lifeProfileMapper;

    private static final Long DEFAULT_USER_ID = 1L;

    @Transactional(rollbackFor = Exception.class)
    public void configureLife(LocalDate birthDate, Integer expectedLifeYears, Integer energyLifeYears) {
        QueryWrapper<LifeProfile> query = new QueryWrapper<>();
        query.eq("user_id", DEFAULT_USER_ID);
        LifeProfile profile = lifeProfileMapper.selectOne(query);

        if (profile == null) {
            profile = new LifeProfile();
            profile.setUserId(DEFAULT_USER_ID);
            profile.setCreatedAt(LocalDateTime.now());
        }
        
        profile.setBirthDate(birthDate);
        profile.setExpectedLifeYears(expectedLifeYears);
        profile.setEnergyLifeYears(energyLifeYears);
        profile.setUpdatedAt(LocalDateTime.now());

        if (profile.getId() == null) {
            lifeProfileMapper.insert(profile);
        } else {
            lifeProfileMapper.updateById(profile);
        }
    }

    public LifeStatusDto getLifeStatus() {
        QueryWrapper<LifeProfile> query = new QueryWrapper<>();
        query.eq("user_id", DEFAULT_USER_ID);
        LifeProfile profile = lifeProfileMapper.selectOne(query);

        if (profile == null) {
            throw new RuntimeException("Life profile not configured yet");
        }

        LocalDate today = LocalDate.now();
        long usedDays = ChronoUnit.DAYS.between(profile.getBirthDate(), today);
        long energyDays = (long) profile.getEnergyLifeYears() * 365; 
        long totalDays = (long) profile.getExpectedLifeYears() * 365;
        
        // Avoid division by zero
        if (totalDays <= 0) totalDays = 1;

        double ratio = (double) usedDays / totalDays;
        
        // Calculate time of day based on ratio
        // 24 hours = 1440 minutes
        int totalMinutes = 1440;
        int usedMinutes = (int) (totalMinutes * ratio);
        
        // Clamp to 24 hours
        if (usedMinutes >= 1440) usedMinutes = 1439;
        if (usedMinutes < 0) usedMinutes = 0;

        LocalTime time = LocalTime.ofSecondOfDay(usedMinutes * 60L);
        String timeStr = time.format(DateTimeFormatter.ofPattern("HH:mm"));

        LifeStatusDto dto = new LifeStatusDto();
        dto.setTotalDays(totalDays);
        dto.setUsedDays(usedDays);
        dto.setEnergyDays(energyDays);
        dto.setUsedRatio(String.format("%.2f%%", ratio * 100));
        dto.setLifeClock(timeStr);
        
        return dto;
    }

    public LifeConfigDto getLifeConfig() {
        QueryWrapper<LifeProfile> query = new QueryWrapper<>();
        query.eq("user_id", DEFAULT_USER_ID);
        LifeProfile profile = lifeProfileMapper.selectOne(query);

        if (profile == null) {
            return null;
        }

        LifeConfigDto dto = new LifeConfigDto();
        dto.setBirthDate(profile.getBirthDate());
        dto.setEnergyLifeYears(profile.getEnergyLifeYears());
        dto.setExpectedLifeYears(profile.getExpectedLifeYears());
        return dto;
    }

    @Data
    public static class LifeStatusDto {
        private Long totalDays;
        private Long energyDays;
        private Long usedDays;
        private String usedRatio;
        private String lifeClock;
    }

    @Data
    public static class LifeConfigDto {
        private LocalDate birthDate;
        private Integer expectedLifeYears;
        private Integer energyLifeYears;
    }
}
