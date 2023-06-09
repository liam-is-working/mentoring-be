package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Department;
import com.example.mentoringapis.entities.UserProfile;
import lombok.Builder;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static com.example.mentoringapis.utilities.DateTimeUtils.VIET_NAM_ZONE;

@Data
@Builder
public class MentorAccountResponse {
    private UUID id;
    private String email;
    private String firebaseUuid;
    private String role;
    private boolean isAuthenticated;
    private String status;
    private String phoneNum;
    private String fullName;
    private String createdDate;

    public static MentorAccountResponse fromAccountEntity(Account account){
        var createdDate = account.getCreatedDate().withZoneSameInstant(VIET_NAM_ZONE).toLocalDateTime();
        return MentorAccountResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .firebaseUuid(account.getFirebaseUuid())
                .isAuthenticated(account.isAuthenticated())
                .role(account.getRole())
                .status(account.getStatus())
                .phoneNum(Optional.of(account.getUserProfile()).map(UserProfile::getPhoneNum).orElse(null))
                .fullName(Optional.of(account.getUserProfile()).map(UserProfile::getFullName).orElse(null))
                .createdDate(createdDate.format(DateTimeFormatter.ofPattern("hh:mm - dd/MM/yyyy")))
                .build();
    }
}
