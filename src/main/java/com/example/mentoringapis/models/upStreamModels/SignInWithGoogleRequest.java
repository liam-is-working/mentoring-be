package com.example.mentoringapis.models.upStreamModels;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Locale;

@Data
public class SignInWithGoogleRequest {
    @Email
    @NotNull
    @NotEmpty
    private String email;
    @NotNull
    @NotEmpty
    private String idToken;
    @NotNull
    @NotEmpty
    private String localId;
    private String fullName;
    private String avatarUrl;

    public String getEmail() {
        return email.toLowerCase(Locale.ROOT);
    }
}
