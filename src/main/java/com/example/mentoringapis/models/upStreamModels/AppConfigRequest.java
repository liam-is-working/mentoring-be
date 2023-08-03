package com.example.mentoringapis.models.upStreamModels;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppConfigRequest {
    private int invitationEmailDelay;
    private int autoRejectBookingDelay;
    private int maxRequestedBooking;
}
