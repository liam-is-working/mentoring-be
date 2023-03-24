package com.example.mentoringapis.models.upStreamModels;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    private String fullName;
    private String description;
    private String dob;
    private String gender;
    private String avatarUrl;
    private String coverUrl;
}
