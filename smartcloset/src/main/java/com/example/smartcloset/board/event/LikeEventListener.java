package com.example.smartcloset.board.event;

import com.example.smartcloset.chat.service.NotificationService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class LikeEventListener implements ApplicationListener<LikeEvent> {

    private final NotificationService notificationService;

    public LikeEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onApplicationEvent(LikeEvent event) {
        // 알림 보내기 로직
        notificationService.sendLikeNotification(event.getPostId());
    }
}