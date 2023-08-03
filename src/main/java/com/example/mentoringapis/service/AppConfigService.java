package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.AppConfig;
import com.example.mentoringapis.models.upStreamModels.AppConfigRequest;
import com.example.mentoringapis.models.upStreamModels.AppConfigResponse;
import com.example.mentoringapis.repositories.AppConfigRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AppConfigService {
    private final AppConfigRepository appConfigRepository;
    private final AppConfig appConfig;

    public List<AppConfigResponse> getAll(){
        return appConfigRepository.findAll()
                .stream().map(AppConfigResponse::fromEntity)
                .toList();
    }

    public AppConfigResponse createNewAppConfig(AppConfigRequest appConfigRequest){
        var newConfig = new AppConfig();
        newConfig.setAutoRejectBookingDelay(appConfigRequest.getAutoRejectBookingDelay());
        newConfig.setMaxRequestedBooking(appConfigRequest.getMaxRequestedBooking());
        newConfig.setInvitationEmailDelay(appConfigRequest.getInvitationEmailDelay());
        appConfigRepository.save(newConfig);

        appConfig.setInvitationEmailDelay(newConfig.getInvitationEmailDelay());
        appConfig.setAutoRejectBookingDelay(newConfig.getAutoRejectBookingDelay());
        appConfig.setMaxRequestedBooking(newConfig.getMaxRequestedBooking());

        return AppConfigResponse.fromEntity(newConfig);
    };
}
