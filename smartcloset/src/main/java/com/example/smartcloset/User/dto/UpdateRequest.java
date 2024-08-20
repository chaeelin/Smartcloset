package com.example.smartcloset.User.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRequest {
    private Integer height;
    private Integer weight;

    public UpdateRequest(Integer height, Integer weight) {
        this.height = height;
        this.weight = weight;
    }
}
