package com.txwang.knowwhy.config;

import com.volcengine.ark.runtime.service.ArkService;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ai")
@Data
public class AiConfig {

    private String apiKey;

    @Bean
    public ArkService aiService() {
        return ArkService.builder().apiKey(apiKey).build();
    }
}
