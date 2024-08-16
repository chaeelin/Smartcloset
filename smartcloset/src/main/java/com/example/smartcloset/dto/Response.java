package com.example.smartcloset.dto;

import org.springframework.http.ResponseEntity;

public class Response {
    public static ResponseEntity<Void> onSuccess() {
        return ResponseEntity.ok().build();
    }

    public static <T> ResponseEntity<T> onSuccess(T body) {
        return ResponseEntity.ok().body(body);
    }
}
