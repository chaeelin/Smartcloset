package com.example.smartcloset.User.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.kakao")
public class KakaoProperties {
    private String clientId;
    private String redirectUri;
}
