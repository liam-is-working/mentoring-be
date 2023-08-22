package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.validation.EnumField;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import javax.annotation.Nullable;

@Data
public class UpdateMentorProfileRequest {
    private String fullName;
    private Integer departmentId;
    @JsonAlias({ "phoneNum", "phoneNumber" })
    private String phoneNum;
    @EnumField(availableValues = {"ACTIVATED", "WAITING", "INVALIDATE"},
    message = "enum: ACTIVATED, WAITING, INVALIDATE")
    private String status;
}
