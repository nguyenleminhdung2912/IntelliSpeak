package com.gsu25se05.itellispeak.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary configKey() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dp39dkdz9");
        config.put("api_key", "541461652994893");
        config.put("api_secret", "-mbd86o7wpucArBlGCpe9AnGSKo");
        return new Cloudinary(config);
    }
}
