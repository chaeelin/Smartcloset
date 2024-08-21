package com.example.smartcloset.chat.controller;

import com.example.smartcloset.chat.dto.ChatGptRequest;
import com.example.smartcloset.chat.dto.ChatGptResponse;
import com.example.smartcloset.chat.service.PostService;
import com.example.smartcloset.chat.service.WeatherService;
import com.example.smartcloset.chat.util.HashTagGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/bot")
public class ChatGptController {

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiURL;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HashTagGenerator hashTagGenerator;

    @Autowired
    private PostService postService;

    @Autowired
    private WeatherService weatherService;

    @GetMapping ("/chat")
    public @ResponseBody Response handleChat(@RequestBody ChatRequest chatRequest) {
        try {
            double latitude = chatRequest.getLatitude();
            double longitude = chatRequest.getLongitude();
            String prompt = chatRequest.getPrompt();

            // 날씨 정보 가져오기
            String weatherInfo = weatherService.getWeatherByCoordinates(latitude, longitude);

            // 프롬프트와 날씨 정보를 결합
            String extendedPrompt = prompt + "\n\n현재 날씨 정보: " + weatherInfo;

            ChatGptRequest request = new ChatGptRequest(model, extendedPrompt);
            ChatGptResponse chatGptResponse = restTemplate.postForObject(apiURL, request, ChatGptResponse.class);

            if (chatGptResponse == null || chatGptResponse.getChoices() == null || chatGptResponse.getChoices().isEmpty()) {
                return new Response("에러: API로부터 응답이 없습니다.");
            }

            String gptResult = chatGptResponse.getChoices().get(0).getMessage().getContent();

            // 해시태그 생성 (선택 사항)
            String hashtags = hashTagGenerator.generateHashTagsFromBoldText(gptResult);

            // 해시태그를 포함한 결과 문자열 생성
            String resultWithHashtags = gptResult + "\n\n #코디'ing #GPT픽 " + hashtags;

            // 게시물 저장 (선택 사항)
            postService.savePost(resultWithHashtags);

            return new Response(resultWithHashtags);
        } catch (Exception e) {
            return new Response("에러가 발생했습니다. " + e.getMessage());
        }
    }

    static class ChatRequest {
        private double latitude;
        private double longitude;
        private String prompt;

        // Getters and Setters
        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }

    static class Response {
        private String response;

        public Response(String response) {
            this.response = response;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }
}
