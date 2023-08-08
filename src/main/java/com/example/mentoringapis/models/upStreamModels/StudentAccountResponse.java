package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Department;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;
import java.util.UUID;

@Data
@Builder
public class StudentAccountResponse {
    private UUID id;
    private String email;
    private String status;
    private String createdDate;
    private String updatedDate;
    private String role;
    private UserProfileResponse profile;


    public static StudentAccountResponse fromAccountEntity(Account account){
        return StudentAccountResponse.builder()
                .id(account.getId())
                .status(account.getStatus())
                .email(account.getEmail())
                .role(account.getRole())
                .createdDate(DateTimeUtils.localDateTimeStringFromZone(account.getUserProfile().getCreatedDate()))
                .updatedDate(DateTimeUtils.localDateTimeStringFromZone(account.getUserProfile().getUpdatedDate()))
                .profile(Optional.ofNullable(account.getUserProfile()).map(UserProfileResponse::fromUserProfileMinimal).orElse(null))
                .build();
    }
}
