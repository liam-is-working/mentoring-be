package com.example.mentoringapis.models.upStreamModels;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UpdateDepartmentRequest {
    private String name;
    private List<UUID> staffIds;
}
