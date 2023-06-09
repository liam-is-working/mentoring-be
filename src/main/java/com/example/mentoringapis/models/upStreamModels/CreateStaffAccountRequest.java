package com.example.mentoringapis.models.upStreamModels;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStaffAccountRequest {
    @Email
    @NotNull
    @NotEmpty
    private String email;
    private String fullName;
    private String avatarUrl;
    @NotNull
    private Integer departmentId;
}
