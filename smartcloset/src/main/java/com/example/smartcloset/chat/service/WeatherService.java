package com.example.smartcloset.chat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${weather.api.url}")
    private String apiUrl;

    @Value("${weather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getWeatherByCoordinates(double latitude, double longitude) {
        String url = String.format("%s?lat=%f&lon=%f&appid=%s", apiUrl, latitude, longitude, apiKey);
        return restTemplate.getForObject(url, String.class);
    }
}
