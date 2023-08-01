package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Department;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.service.StaticResourceService;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.Builder;
import lombok.Data;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.example.mentoringapis.utilities.DateTimeUtils.VIET_NAM_ZONE;
import static java.util.Optional.ofNullable;

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
    private String avatarUrl;
    private String avatarLink;

    public static MentorAccountResponse fromAccountEntity(Account account){
        var createdDate = account.getCreatedDate().withZoneSameInstant(VIET_NAM_ZONE).toLocalDateTime();
        return MentorAccountResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .role(account.getRole())
                .avatarUrl(account.getUserProfile().getAvatarUrl())
                .avatarLink(account.getUserProfile().getAvatarUrl())
                .status(account.getStatus())
                .phoneNum(account.getUserProfile().getPhoneNum())
                .fullName(account.getUserProfile().getFullName())
                .createdDate(createdDate.format(DateTimeFormatter.ofPattern(DateTimeUtils.DEFAULT_DATE_TIME_PATTERN)))
                .build();
    }
}
