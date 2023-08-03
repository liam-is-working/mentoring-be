package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Department;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class AccountResponse {
    private UUID id;
    private String email;
    private String role;
    private String status;
    private String createdDate;
    private String updatedDate;
    private boolean isAuthenticated;

    public static AccountResponse fromAccountEntity(Account account){
        return AccountResponse.builder()
                .id(account.getId())
                .createdDate(account.getCreatedDate().toLocalDateTime().format(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER))
                .updatedDate(DateTimeUtils.localDateTimeStringFromZone(account.getUpdatedDate()))
                .role(account.getRole())
                .email(account.getEmail())
                .build();
    }

}
