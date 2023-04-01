package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.Cv;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.models.upStreamModels.CvInformation;
import com.example.mentoringapis.models.upStreamModels.UserProfileResponse;
import com.example.mentoringapis.models.upStreamModels.UserProfileUpdateRequest;
import com.example.mentoringapis.repositories.UserProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;

    public UserProfileResponse update(UserProfileUpdateRequest request, UUID profileId){
        return userProfileRepository.findUserProfileByAccount_Id(profileId)
                .map(prof -> {
                    prof.setFullName(request.getFullName());
                    prof.setGender(request.getGender());
                    prof.setAvatarUrl(request.getAvatarUrl());
                    prof.setCoverUrl(request.getCoverUrl());
                    prof.setDescription(request.getDescription());
                    if(request.getDob() != null){
                        prof.setDob(Date.valueOf(request.getDob()));
                    }
                    return userProfileRepository.save(prof);
                }).map(UserProfileResponse::fromUserProfile).orElse(null);
    }

    public UserProfileResponse findByUUID(UUID id){
        return userProfileRepository
                .findUserProfileByAccount_Id(id)
                .map(UserProfileResponse::fromUserProfile)
                .orElse(null);
    }

    public String findCvByUUID(UUID id){
        return userProfileRepository
                .findUserProfileByAccount_Id(id)
                .map(userProfile -> userProfile.getCv()).orElse(null);
    }

    public UserProfile updateCv(CvInformation cv, UUID profileId){
        return userProfileRepository.findUserProfileByAccount_Id(profileId)
                .map(profileToUpdate -> {
                    try {
                        profileToUpdate.setCv(objectMapper.writeValueAsString(cv));
                        return userProfileRepository.save(profileToUpdate);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).orElse(null);
    }
}
