package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.Gender;
import com.example.mentoringapis.validation.CheckStringDate;
import com.example.mentoringapis.validation.EnumField;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountUpdateRequest {
    @EnumField(availableValues = {"ACTIVATED", "INVALIDATE"},
            message = "enum: ACTIVATED, INVALIDATE")
    @NotEmpty
    private String status;
}
