package com.example.mentoringapis.models.upStreamModels;

import lombok.Data;

@Data
public class DeleteScheduleRequest {
    Long scheduleId = null;
    String startTime;
    String options;
    public enum Option{
        ALL, ONLY
    }
}
