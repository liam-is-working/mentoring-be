package com.example.mentoringapis.models.upStreamModels;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotNull
    @NotEmpty
    String oobCode;


    @NotNull
    @NotEmpty
    @Size(max = 100)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "Minimum eight characters, at least one letter, one number and one special character")
    private String password;
}
