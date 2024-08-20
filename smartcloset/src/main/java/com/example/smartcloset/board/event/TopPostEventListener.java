package com.example.smartcloset.board.event;

import com.example.smartcloset.chat.service.NotificationService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class TopPostEventListener implements ApplicationListener<TopPostEvent> {

    private final NotificationService notificationService;

    public TopPostEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onApplicationEvent(TopPostEvent event) {
        // 상단 노출 알림 보내기 로직
        notificationService.sendTopPostNotification(event.getPostId());
    }
}
