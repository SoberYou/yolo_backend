package com.life.yolo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.life.yolo.mapper")
public class YoloApplication {

    public static void main(String[] args) {
        SpringApplication.run(YoloApplication.class, args);
    }
}
