package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Gender;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdateRequest {
    @NotNull
    private String fullName;
    private String description;
    private LocalDate dob;
    private Gender gender;
    private String avatarUrl;
    private String coverUrl;
}
