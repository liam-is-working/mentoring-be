package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Department;
import com.example.mentoringapis.entities.Gender;
import com.example.mentoringapis.security.JwtTokenProvider;
import lombok.Builder;
import lombok.Data;

import java.sql.Date;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
public class SignInRes {
    String email;
    UUID accountId;
    String status;
    String accessToken;
    String role;
    boolean isLocked;
    String sex;
    String fullName;
    String dob;
    String avatarUrl;
    Integer departmentId;
    boolean isAuthenticated;

    public static SignInRes buildFromAccount(Account account, JwtTokenProvider jwtTokenProvider){
        var userProfile = account.getUserProfile();
        var accessToken = jwtTokenProvider.generateToken(account.getId());
        return SignInRes.builder()
                .accessToken(accessToken)
                .accountId(account.getId())
                .status(account.getStatus())
                .avatarUrl(userProfile.getAvatarUrl())
                .dob(String.valueOf(userProfile.getDob()))
                .email(account.getEmail())
                .sex(Optional.ofNullable(userProfile.getGender()).map(Enum::name).orElse(Gender.others.name()))
                .isLocked(false)
                .departmentId(Optional.ofNullable(account.getDepartment()).map(Department::getId).orElse(null))
                .role(account.getRole())
                .fullName(userProfile.getFullName())
                .isAuthenticated(account.isAuthenticated())
                .build();
    }
}
