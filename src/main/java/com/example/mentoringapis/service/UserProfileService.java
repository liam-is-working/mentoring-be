package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.models.upStreamModels.UserProfileUpdateRequest;
import com.example.mentoringapis.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;

    public UserProfile update(UserProfileUpdateRequest request, UUID profileId){
        return userProfileRepository.findUserProfileByAccount_Id(profileId)
                .map(prof -> {
                    prof.setFullName(request.getFullName());
                    prof.setDob(request.getDob());
                    prof.setGender(request.getGender());
                    prof.setAvatarUrl(request.getAvatarUrl());
                    prof.setCoverUrl(request.getCoverUrl());
                    prof.setDescription(request.getDescription());

                    return userProfileRepository.save(prof);
                }).orElse(null);

    }
}
