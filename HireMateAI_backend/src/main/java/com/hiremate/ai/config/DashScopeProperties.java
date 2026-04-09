package com.hiremate.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DashScope AI 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeProperties {

    private String apiKey;

    private String model;

    private String baseUrl;
}
