package com.example.mentoringapis.models.upStreamModels;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMailResetPasswordRequest {

    @NotNull
    @Email
    @NotEmpty
    String email;
}
