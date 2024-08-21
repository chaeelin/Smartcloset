package com.example.smartcloset.User.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeRequest {
    private String newNickname;
    private String currentPassword;
    private String newPassword;

    public ChangeRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}
