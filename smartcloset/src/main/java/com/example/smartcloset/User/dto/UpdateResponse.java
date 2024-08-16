package com.example.smartcloset.User.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateResponse {
    private Integer height;
    private Integer weight;

    public UpdateResponse(Integer height, Integer weight) {
        this.height = height;
        this.weight = weight;
    }
}
