package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.validation.EnumField;
import lombok.Data;

import javax.annotation.Nullable;

@Data
public class UpdateMentorProfileRequest {
    private String fullName;
    private String phoneNum;
    @EnumField(availableValues = {"ACTIVATED", "WAITING", "INVALIDATE"},
    message = "enum: ACTIVATED, WAITING, INVALIDATE")
    private String status;
}
