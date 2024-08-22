package com.example.smartcloset.board.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TopPostEvent extends ApplicationEvent {
    private final Long postId;

    public TopPostEvent(Object source, Long postId) {
        super(source);
        this.postId = postId;
    }

}