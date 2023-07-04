package com.example.mentoringapis.models.upStreamModels;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateDepartmentRequest {
    private String name;
    private List<UUID> staffIds;
}
