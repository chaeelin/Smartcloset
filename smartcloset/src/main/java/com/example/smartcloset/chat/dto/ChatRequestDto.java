package com.example.smartcloset.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequestDto {
    private double latitude;
    private double longitude;
    private String prompt;
}
