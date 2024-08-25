package com.example.smartcloset.chat.controller;

import com.example.smartcloset.chat.dto.ChatGptRequest;
import com.example.smartcloset.chat.dto.ChatGptResponse;
import com.example.smartcloset.chat.dto.Response;
import com.example.smartcloset.chat.service.PostService;
import com.example.smartcloset.chat.service.WeatherService;
import com.example.smartcloset.chat.util.HashTagGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/bot")
public class ChatGptController {
    private static final Logger logger = LoggerFactory.getLogger(ChatGptController.class);

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

    @GetMapping("/chat")
    public @ResponseBody Response handleChat(
            @RequestParam(name = "latitude", required = false, defaultValue = "37.3951722") double latitude,
            @RequestParam(name = "longitude", required = false, defaultValue = "126.9443105") double longitude,
            @RequestParam(name = "prompt", defaultValue = "오늘 뭐 입을까?") String prompt) {
        try {
            // 위도와 경도 로그 출력
            logger.info("Received request with latitude: {}, longitude: {}", latitude, longitude);

            // 날씨 정보 가져오기
            String weatherInfo = weatherService.getWeatherByCoordinates(latitude, longitude);

            // 프롬프트와 날씨 정보를 결합
            String extendedPrompt = prompt + "\n\n현재 날씨 정보: " + weatherInfo;

            // ChatGPT API 요청 생성
            // 이 부분에서 모델과 프롬프트를 사용하여 ChatGptRequest 객체를 생성합니다.
            // extendedPrompt를 사용하여 요청을 구성합니다.
            ChatGptRequest request = new ChatGptRequest(model, extendedPrompt);

            // ChatGPT API 호출
            ChatGptResponse chatGptResponse = restTemplate.postForObject(apiURL, request, ChatGptResponse.class);

            // 응답 검증
            if (chatGptResponse == null || chatGptResponse.getChoices() == null || chatGptResponse.getChoices().isEmpty()) {
                // API로부터 유효한 응답이 없을 경우
                return new Response("에러: API로부터 응답이 없습니다.");
            }

            // GPT 응답 결과 가져오기
            String gptResult = chatGptResponse.getChoices().get(0).getMessage().getContent();

            // 해시태그 생성 (선택 사항)
            String hashtags = hashTagGenerator.generateHashTagsFromBoldText(gptResult);

            // 해시태그를 포함한 결과 문자열 생성
            String resultWithHashtags = gptResult + "\n\n #코디'ing #GPT픽 " + hashtags;

            // 게시물 저장 (선택 사항)
            postService.savePost(resultWithHashtags);

            return new Response(resultWithHashtags);
        } catch (Exception e) {
            // 에러 발생 시 로그 기록 및 응답 반환
            logger.error("Error occurred: {}", e.getMessage(), e);
            return new Response("에러가 발생했습니다. " + e.getMessage());
        }
    }
}
