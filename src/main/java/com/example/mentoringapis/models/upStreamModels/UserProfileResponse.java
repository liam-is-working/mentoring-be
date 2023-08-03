package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Department;
import com.example.mentoringapis.entities.MeetingFeedback;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.service.StaticResourceService;
import com.example.mentoringapis.utilities.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.net.URL;
import java.sql.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

import static java.util.Optional.ofNullable;

@Getter
@Setter
@Builder
public class UserProfileResponse {
    private String accountId;
    private String id;
    private String fullName;
    private String description;
    private String dob;
    private String createdDate;
    private String updatedDate;
    private String gender;
    private String phone;
    private String email;
    private String status;
    private String departmentName;
    private Integer departmentId;
    private String avatarUrl;
    private String avatarLink;
    private String coverUrl;
    private String coverLink;
    private String role;
    private String ratingString;
    private Integer followers;

    @JsonIgnore
    private OptionalDouble ratingOptional;
    @JsonGetter()
    public String getRatingString(){
        if(ratingOptional!= null && ratingOptional.isPresent())
            return String.format("%.2f", ratingOptional.getAsDouble());
        return null;
    }



    public static UserProfileResponse fromUserProfile(UserProfile userProfile){
        return UserProfileResponse.builder()
                .followers(userProfile.getFollowers().size())
                .ratingOptional(userProfile.getFeedbacks().stream().mapToInt(MeetingFeedback::getRating).average())
                .id(userProfile.getAccountId().toString())
                .departmentName(ofNullable(userProfile.getAccount().getDepartment()).map(Department::getName).orElse(null))
                .departmentId(ofNullable(userProfile.getAccount().getDepartment()).map(Department::getId).orElse(null))
                .phone(userProfile.getPhoneNum())
                .email(userProfile.getAccount().getEmail())
                .status(userProfile.getAccount().getStatus())
                .description(userProfile.getDescription())
                .createdDate(DateTimeUtils.localDateTimeStringFromZone(userProfile.getCreatedDate()))
                .updatedDate(DateTimeUtils.localDateTimeStringFromZone(userProfile.getUpdatedDate()))
                .accountId(userProfile.getAccountId().toString())
                .avatarUrl(userProfile.getAvatarUrl())
                .role(userProfile.getAccount().getRole())
                .coverUrl(userProfile.getCoverUrl())
                .role(userProfile.getAccount().getRole())
                .gender(ofNullable(userProfile.getGender()).map(Enum::name).orElse(null))
                .fullName(userProfile.getFullName())
                .dob(ofNullable(userProfile.getDob()).map(Date::toLocalDate).map(Objects::toString).orElse(null))
                .avatarLink(userProfile.getAvatarUrl())
                .coverLink(userProfile.getCoverUrl())
                .build();
    }

    public static UserProfileResponse fromUserProfileMinimal(UserProfile userProfile){
        return UserProfileResponse.builder()
                .id(userProfile.getAccountId().toString())
                .phone(userProfile.getPhoneNum())
                .email(userProfile.getAccount().getEmail())
                .status(userProfile.getAccount().getStatus())
                .createdDate(DateTimeUtils.localDateTimeStringFromZone(userProfile.getCreatedDate()))
                .updatedDate(DateTimeUtils.localDateTimeStringFromZone(userProfile.getUpdatedDate()))
                .description(userProfile.getDescription())
                .accountId(userProfile.getAccountId().toString())
                .avatarUrl(userProfile.getAvatarUrl())
                .role(userProfile.getAccount().getRole())
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
