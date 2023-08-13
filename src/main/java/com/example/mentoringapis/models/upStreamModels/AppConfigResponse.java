package com.example.mentoringapis.models.upStreamModels;

import com.example.mentoringapis.entities.AppConfig;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppConfigResponse {
    private int id;
    private int invitationEmailDelay;
    private int autoRejectBookingDelay;
    private int maxRequestedBooking;
    private int maxParticipant;
    private int maxCallDuration;
    private String createdDate;

    public static AppConfigResponse fromEntity(AppConfig entity){
        return AppConfigResponse.builder()
                .id(entity.getId())
                .maxCallDuration(entity.getMaxCallDuration())
                .maxParticipant(entity.getMaxParticipant())
                .autoRejectBookingDelay(entity.getAutoRejectBookingDelay())
                .invitationEmailDelay(entity.getSeminarReportEmailDelay())
                .maxRequestedBooking(entity.getMaxRequestedBooking())
                .createdDate((DateTimeUtils.localDateTimeStringFromZone(entity.getCreatedDate())))
                .build();
    }
}
