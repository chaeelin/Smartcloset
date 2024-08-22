package com.example.smartcloset.chat.config;


import com.example.smartcloset.chat.util.HashTagGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HashTagConfig {

    @Bean
    public HashTagGenerator hashTagGenerator() {
        return new HashTagGenerator();
    }
}