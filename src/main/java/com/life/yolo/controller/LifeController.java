package com.life.yolo.controller;

import com.life.yolo.common.ApiResponse;
import com.life.yolo.service.LifeService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/life")
public class LifeController {

    @Autowired
    private LifeService lifeService;

    @PostMapping("/configLife")
    public ApiResponse<Void> configLife(@RequestBody LifeConfigRequest request) {
        lifeService.configureLife(request.getBirthDate(), request.getExpectedLifeYears(), request.getEnergyLifeYears());
        return ApiResponse.success(null);
    }

    @GetMapping("/getLifeConfig")
    public ApiResponse<LifeService.LifeConfigDto> getLifeConfig() {
        return ApiResponse.success(lifeService.getLifeConfig());
    }

    @GetMapping("/getLifeStatus")
    public ApiResponse<LifeService.LifeStatusDto> getLifeStatus() {
        return ApiResponse.success(lifeService.getLifeStatus());
    }

    @Data
    public static class LifeConfigRequest {
        private LocalDate birthDate;
        private Integer expectedLifeYears;
        private Integer energyLifeYears;
    }
}
