package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Gender;
import com.example.mentoringapis.security.JwtTokenProvider;
import lombok.Builder;
import lombok.Data;

import java.sql.Date;
import java.util.Optional;

@Data
@Builder
public class SignInRes {
    String email;
    String accessToken;
    String role;
    boolean isLocked;
    String sex;
    String fullName;
    String dob;
    String avatarUrl;
    boolean isAuthenticated;

    public static SignInRes buildFromAccount(Account account, JwtTokenProvider jwtTokenProvider){
        var userProfile = account.getUserProfile();
        var accessToken = jwtTokenProvider.generateToken(account.getId());
        return SignInRes.builder()
                .accessToken(accessToken)
                .avatarUrl(userProfile.getAvatarUrl())
                .dob(String.valueOf(userProfile.getDob()))
                .email(account.getEmail())
                .sex(Optional.ofNullable(userProfile.getGender()).map(Enum::name).orElse(Gender.others.name()))
                .isLocked(false)
                .role(account.getRole())
                .fullName(userProfile.getFullName())
                .isAuthenticated(account.isAuthenticated())
                .build();
    }
}
