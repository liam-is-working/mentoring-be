package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.service.StaticResourceService;
import lombok.*;

import java.net.URL;
import java.sql.Date;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Getter
@Setter
@Builder
public class UserProfileResponse {
    private String accountId;
    private String fullName;
    private String description;
    private String dob;
    private String gender;
    private String avatarUrl;
    private String avatarLink;
    private String coverUrl;
    private String coverLink;
    private String role;



    public static UserProfileResponse fromUserProfile(UserProfile userProfile, StaticResourceService staticResourceService){
        return UserProfileResponse.builder()
                .description(userProfile.getDescription())
                .avatarUrl(userProfile.getAvatarUrl())
                .coverUrl(userProfile.getCoverUrl())
                .role(userProfile.getAccount().getRole())
                .gender(ofNullable(userProfile.getGender()).map(Enum::name).orElse(null))
                .fullName(userProfile.getFullName())
                .dob(ofNullable(userProfile.getDob()).map(Date::toLocalDate).map(Objects::toString).orElse(null))
                .avatarLink(userProfile.getAvatarUrl())
                .coverLink(userProfile.getCoverUrl())
                .build();

    }
}
