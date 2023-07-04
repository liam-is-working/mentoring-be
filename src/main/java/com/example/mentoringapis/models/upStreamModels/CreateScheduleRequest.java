package com.example.mentoringapis.models.upStreamModels;

import lombok.Data;

@Data
public class CreateScheduleRequest {
    String startTime;
    Boolean daily = false;
    Boolean weekly = false;
}
