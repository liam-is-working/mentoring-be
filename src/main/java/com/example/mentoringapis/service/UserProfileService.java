package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.repositories.UserProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final FireStoreService fireStoreService;
    private final StaticResourceService staticResourceService;
    private final ObjectMapper objectMapper;

    public MentorAccountResponse update(UpdateMentorProfileRequest request, UUID mentorId) throws ResourceNotFoundException {
        var mentorToUpdate = userProfileRepository.findUserProfileByAccount_Id(mentorId);
        return mentorToUpdate.map(
                profile -> {
                    var account = profile.getAccount();
                    Optional.ofNullable(request.getPhoneNum()).ifPresent(profile::setPhoneNum);
                    Optional.ofNullable(request.getFullName()).ifPresent(profile::setFullName);
                    Optional.ofNullable(request.getStatus()).ifPresent(account::setStatus);
                    userProfileRepository.save(profile);

                    fireStoreService.updateUserProfile(request.getFullName(), null, account.getRole(), account.getEmail(),mentorId);

                    return MentorAccountResponse.fromAccountEntity(account);
                }
        ).orElseThrow(() -> new ResourceNotFoundException(String.format("Cannot find mentor with id: %s", mentorId)));
    }

    public UserProfileResponse update(UserProfileUpdateRequest request, UUID profileId){
        return userProfileRepository.findUserProfileByAccount_Id(profileId)
                .map(prof -> {
                    prof.setFullName(request.getFullName());
                    prof.setGender(request.getGender());
                    prof.setAvatarUrl(request.getAvatarUrl());
                    prof.setCoverUrl(request.getCoverUrl());
                    prof.setDescription(request.getDescription());
                    if(request.getActivateAccount())
                        prof.getAccount().setStatus(Account.Status.ACTIVATED.name());
                    if(request.getDob() != null){
                        prof.setDob(Date.valueOf(request.getDob()));
                    }

                    fireStoreService.updateUserProfile(request.getFullName(), request.getAvatarUrl(),
                            prof.getAccount().getRole(), prof.getAccount().getEmail(), profileId);
                    return userProfileRepository.save(prof);
                }).map(prof -> UserProfileResponse.fromUserProfile(prof, staticResourceService)).orElse(null);
    }

    public UserProfileResponse findByUUID(UUID id){
        return userProfileRepository
                .findUserProfileByAccount_Id(id)
                .map(prof -> UserProfileResponse.fromUserProfile(prof, staticResourceService))
                .orElse(null);
    }

    public String findCvByUUID(UUID id){
        return userProfileRepository
                .findUserProfileByAccount_Id(id)
                .map(UserProfile::getCv).orElse("");
    }

    public CvInformationResponse getCvResponseByUUID(UUID uuid){
        var cvString = findCvByUUID(uuid);
        try {
            var cvResponse = objectMapper.readValue(cvString, CvInformationResponse.class);
            cvResponse.setUserProfileId(uuid);
            return cvResponse;
        } catch (JsonProcessingException e) {
            //TODO log
            return null;
        }
    }

    public CvInformationResponse updateCv(CvInformationUpdateRequest cv, UUID profileId){
        return userProfileRepository.findUserProfileByAccount_Id(profileId)
                .map(profileToUpdate -> {
                    try {
                        profileToUpdate.setCv(objectMapper.writeValueAsString(cv));
                        return userProfileRepository.save(profileToUpdate);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .map(UserProfile::getCv)
                .map(cvString -> {
                    try {
                        var cvRes = objectMapper.readValue(cvString, CvInformationResponse.class);
                        cvRes.setUserProfileId(profileId);
                        return cvRes;
                    } catch (JsonProcessingException e) {
                        //TODO log
                        return null;
                    }
                })
                .orElse(null);
    }
}
