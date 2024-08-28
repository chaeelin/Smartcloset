package com.example.smartcloset.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {
    private String targetToken;
    private String title;
    private String body;
}