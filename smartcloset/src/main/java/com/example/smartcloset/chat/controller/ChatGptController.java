package com.example.smartcloset.chat.controller;

import com.example.smartcloset.chat.dto.ChatGptRequest;
import com.example.smartcloset.chat.dto.ChatGptResponse;
import com.example.smartcloset.chat.service.PostService;
import com.example.smartcloset.chat.util.HashTagGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/bot")
public class ChatGptController {

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiURL;

    @Autowired
    private RestTemplate template;

    @Autowired
    private HashTagGenerator hashTagGenerator;  // HashTagGenerator를 주입받음

    @Autowired
    private PostService postService;  // PostService를 주입받음

    @GetMapping("/chat")
    public String chat(@RequestParam("prompt") String prompt) {
        try {
            ChatGptRequest request = new ChatGptRequest(model, prompt);
            ChatGptResponse chatGptResponse = template.postForObject(apiURL, request, ChatGptResponse.class);

            if (chatGptResponse == null || chatGptResponse.getChoices() == null || chatGptResponse.getChoices().isEmpty()) {
                return "에러: API로부터 응답이 없습니다.";
            }

            String gptResult = chatGptResponse.getChoices().get(0).getMessage().getContent();

            // 해시태그 생성
            String hashtags = hashTagGenerator.generateHashTagsFromBoldText(gptResult);

            // 해시태그를 포함한 결과 문자열 생성
            String resultWithHashtags = gptResult + "\n\n #코디'ing #GPT픽 " + hashtags;

            // 게시물 저장
            postService.savePost(resultWithHashtags);

            return resultWithHashtags;
        } catch (Exception e) {
            // 에러 메시지를 로그로 남기고 사용자에게 표시
            return "에러가 발생했습니다. " + e.getMessage();
        }
    }
}
