package com.example.mentoringapis.models.upStreamModels;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignInWithPasswordRequest {
    @Email
    @NotEmpty
    @NotNull
    private String email;
    @NotEmpty
    @NotNull
    private String password;
}
