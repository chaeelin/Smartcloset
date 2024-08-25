package com.example.smartcloset.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String role;
    private String content; // 프롬프트와 같은 내용
    private String response; // 추가 필드

    // 두 개의 인자를 받는 생성자 추가
    public Message(String role, String content) {
        this.role = role;
        this.content = content;
        this.response = ""; // 기본값 설정
    }
}
