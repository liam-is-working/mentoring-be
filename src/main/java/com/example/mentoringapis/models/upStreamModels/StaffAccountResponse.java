package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Department;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;
import java.util.UUID;

@Data
@Builder
public class StaffAccountResponse {
    private UUID id;
    private String email;
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
                .email(account.getEmail())
                .firebaseUuid(account.getFirebaseUuid())
                .isAuthenticated(account.isAuthenticated())
                .role(account.getRole())
                .department(Optional.ofNullable(account.getDepartment()).map(DepartmentRes::fromDepartment).orElse(null))
                .profile(Optional.ofNullable(account.getUserProfile()).map(prof -> UserProfileResponse.fromUserProfile(prof, null)).orElse(null))
                .build();
    }
}
