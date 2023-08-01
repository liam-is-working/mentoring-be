package com.example.mentoringapis.models.upStreamModels;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateMeetingFeedbackRequest {
    private String content;
    @Min(0)
    @Max(5)
    private int rating;
    private UUID receiver;
}
