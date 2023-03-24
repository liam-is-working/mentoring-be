package com.example.mentoringapis.models.upStreamModels;

import com.google.firebase.database.annotations.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SignUpWithEmailPasswordRequest {

    @NotNull
    @Email
    @Size(max = 255)
    private String email;

    @NotNull
    @NotEmpty
    private String fullName;

    @NotNull
    @NotEmpty
    @Size(max = 100)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "Minimum eight characters, at least one letter, one number and one special character")
    private String password;


}
