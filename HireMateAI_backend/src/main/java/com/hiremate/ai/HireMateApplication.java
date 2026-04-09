package com.hiremate.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.hiremate.ai.mapper")
@ComponentScan(basePackages = {"com.hiremate.ai", "com.hiremate.ai.ai"})
public class HireMateApplication {

    public static void main(String[] args) {
        SpringApplication.run(HireMateApplication.class, args);
    }
}
