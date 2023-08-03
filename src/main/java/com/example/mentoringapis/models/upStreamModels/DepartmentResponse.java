package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Department;
import com.example.mentoringapis.entities.Seminar;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class DepartmentResponse {
    private int id;
    private String name;
    private String createdDate;
    private String updatedDate;
    private Set<UUID> staffIds;
    private Set<Long> seminarIds;
    private Set<UserProfileResponse> staffs;

    public static DepartmentResponse fromDepartment(Department department){
        return DepartmentResponse.builder()
                .id(department.getId())
                .createdDate(DateTimeUtils.localDateTimeStringFromZone(department.getCreatedAt()))
                .updatedDate(DateTimeUtils.localDateTimeStringFromZone(department.getUpdatedDate()))
                .name(department.getName())
                .seminarIds(department.getSeminars().stream().map(Seminar::getId).collect(Collectors.toSet()))
                .staffIds(department.getStaffAccounts().stream().map(Account::getId).collect(Collectors.toSet()))
                .staffs(department.getStaffAccounts().stream().map(Account::getUserProfile).map(UserProfileResponse::fromUserProfile).collect(Collectors.toSet()))
                .build();
    }
}
