package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.security.JwtTokenProvider;
import lombok.Builder;
import lombok.Data;

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

    public static SignInRes buildFromAccount(Account account, JwtTokenProvider jwtTokenProvider){
        var userProfile = account.getUserProfile();
        var accessToken = jwtTokenProvider.generateToken(account.getId());
        return SignInRes.builder()
                .accessToken(accessToken)
                .avatarUrl(userProfile.getAvatarUrl())
                .dob(userProfile.getDob())
                .email(account.getEmail())
                .sex(userProfile.getGender())
                .isLocked(false)
                .role(account.isMentor() ? "MENTOR" : "MENTEE")
                .fullName(userProfile.getFullName())
                .build();
    }
}
