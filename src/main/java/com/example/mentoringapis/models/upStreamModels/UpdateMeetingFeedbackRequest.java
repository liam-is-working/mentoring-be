package com.example.mentoringapis.models.upStreamModels;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateMeetingFeedbackRequest {
    private String content;
    @Min(0)
    @Max(5)
    private Integer rating;
    private Boolean delete = false;
}
