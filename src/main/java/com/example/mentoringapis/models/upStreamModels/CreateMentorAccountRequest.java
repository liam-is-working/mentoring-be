package com.example.mentoringapis.models.upStreamModels;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

@Getter
@Setter
public class CreateMentorAccountRequest {
    @Email
    @NotNull
    @NotEmpty
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;

    public String getEmail() {
        return email.toLowerCase(Locale.ROOT);
    }
}
