package com.fitlife.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // Khởi tạo công cụ RestTemplate và giao cho Spring Boot quản lý (Bean)
    // Từ giờ, bất cứ Service nào cần gọi API bên ngoài chỉ cần @RequiredArgsConstructor là dùng được ngay.
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}