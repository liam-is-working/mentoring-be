package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.AppConfig;
import com.example.mentoringapis.models.upStreamModels.AppConfigRequest;
import com.example.mentoringapis.models.upStreamModels.AppConfigResponse;
import com.example.mentoringapis.repositories.AppConfigRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class AppConfigService {
    private final AppConfigRepository appConfigRepository;
    private final AppConfig appConfig;

    public AppConfigResponse getApplied(){
        return appConfigRepository.findAll()
                .stream()
                .max(Comparator.comparing(AppConfig::getCreatedDate))
                .map(AppConfigResponse::fromEntity)
                .orElse(AppConfigResponse.builder().build());
    }

    public AppConfigResponse createNewAppConfig(AppConfigRequest appConfigRequest){
        var newConfig = new AppConfig();
        newConfig.setAutoRejectBookingDelay(appConfigRequest.getAutoRejectBookingDelay());
        newConfig.setMaxRequestedBooking(appConfigRequest.getMaxRequestedBooking());
        newConfig.setSeminarReportEmailDelay(appConfigRequest.getInvitationEmailDelay());
        newConfig.setMaxParticipant(appConfigRequest.getMaxParticipant());
        newConfig.setMaxCallDuration(appConfigRequest.getMaxCallDuration());
        appConfigRepository.save(newConfig);

        appConfig.setSeminarReportEmailDelay(newConfig.getSeminarReportEmailDelay());
        appConfig.setAutoRejectBookingDelay(newConfig.getAutoRejectBookingDelay());
        appConfig.setMaxRequestedBooking(newConfig.getMaxRequestedBooking());
        appConfig.setMaxParticipant(newConfig.getMaxParticipant());
        appConfig.setMaxCallDuration(newConfig.getMaxCallDuration());

        return AppConfigResponse.fromEntity(newConfig);
    };
}
