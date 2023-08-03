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
public class StaffAccountResponse {
    private UUID id;
    private String email;
    private String status;
    private String createdDate;
    private String updatedDate;
    private String firebaseUuid;
    private String role;
    private boolean isAuthenticated;
    private DepartmentRes department;
    private UserProfileResponse profile;

    @Data
    @Builder
    private static class DepartmentRes{
        private int id;
        private String name;

        static DepartmentRes fromDepartment(Department department){
            return DepartmentRes.builder()
                    .id(department.getId())
                    .name(department.getName())
                    .build();
        }
    }


    public static StaffAccountResponse fromAccountEntity(Account account){
        return StaffAccountResponse.builder()
                .id(account.getId())
                .status(account.getStatus())
                .email(account.getEmail())
                .role(account.getRole())
                .createdDate(DateTimeUtils.localDateTimeStringFromZone(account.getUserProfile().getCreatedDate()))
                .updatedDate(DateTimeUtils.localDateTimeStringFromZone(account.getUserProfile().getUpdatedDate()))
                .department(Optional.ofNullable(account.getDepartment()).map(DepartmentRes::fromDepartment).orElse(null))
                .profile(Optional.ofNullable(account.getUserProfile()).map(prof -> UserProfileResponse.fromUserProfile(prof)).orElse(null))
                .build();
    }
}
