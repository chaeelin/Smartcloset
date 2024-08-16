package com.example.smartcloset.User.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckResponse {
    private boolean available;

    public CheckResponse(boolean available) {
        this.available = available;
    }
}
