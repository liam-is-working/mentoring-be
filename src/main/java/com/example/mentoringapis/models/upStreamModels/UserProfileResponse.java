package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.UserProfile;
import lombok.*;

import java.sql.Date;
import java.util.Objects;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileResponse {
    private String accountId;
    private String fullName;
    private String description;
    private String dob;
    private String gender;
    private String avatarUrl;
    private String coverUrl;



    public static UserProfileResponse fromUserProfile(UserProfile userProfile){
        var returnProfile = new UserProfileResponse();
        returnProfile.setAvatarUrl(userProfile.getAvatarUrl());
        returnProfile.setCoverUrl(userProfile.getCoverUrl());
        returnProfile.setGender(Optional.ofNullable(userProfile.getGender()).map(Enum::name).orElse(""));
        returnProfile.setAccountId(userProfile.getAccountId().toString());
        returnProfile.setDescription(userProfile.getDescription());
        returnProfile.setFullName(userProfile.getFullName());
        returnProfile.setDob(Optional.ofNullable(userProfile.getDob()).map(Date::toLocalDate).map(Objects::toString).orElse(""));
        return returnProfile;
    }
}
