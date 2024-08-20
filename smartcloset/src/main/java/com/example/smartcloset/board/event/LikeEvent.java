package com.example.smartcloset.board.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LikeEvent extends ApplicationEvent {
    private final Long postId;

    public LikeEvent(Object source, Long postId) {
        super(source);
        this.postId = postId;
    }

}