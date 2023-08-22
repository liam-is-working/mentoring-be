package com.example.mentoringapis.models.upStreamModels;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppConfigRequest {
    private int seminarReportEmailDelay;
    private int invitationEmailDelay;
    private int autoRejectBookingDelay;
    private int maxRequestedBooking;
    private int maxParticipant;
    private int maxCallDuration;
    private int reminderEmailDelay;
    private int maxMentorRecommendation;
}
