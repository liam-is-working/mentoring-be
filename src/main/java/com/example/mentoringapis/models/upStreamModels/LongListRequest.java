package com.example.mentoringapis.models.upStreamModels;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class LongListRequest {
    @NotNull
    @NotEmpty
    List<Long> ids;
}
