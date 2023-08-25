package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Gender;
import com.example.mentoringapis.validation.CheckStringDate;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    @NotNull
    private String fullName;
    private String description;
    @CheckStringDate
    private String dob;
    private Gender gender;
    private String phone;
    private String avatarUrl;
    private String coverUrl;
    private Boolean activateAccount = false;
}
