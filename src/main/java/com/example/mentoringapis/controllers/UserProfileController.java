package com.example.mentoringapis.controllers;

import com.example.mentoringapis.models.upStreamModels.CvInformationResponse;
import com.example.mentoringapis.models.upStreamModels.CvInformationUpdateRequest;
import com.example.mentoringapis.models.upStreamModels.UserProfileResponse;
import com.example.mentoringapis.models.upStreamModels.UserProfileUpdateRequest;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.service.UserProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "user-profile")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/current")
    public UserProfileResponse updateProfile(@Valid @RequestBody UserProfileUpdateRequest request, Authentication authentication){
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        return userProfileService.update(request, currentUser.getAccount().getId());
    }

    @RequestMapping(path = "/cv/current", method = RequestMethod.POST)
    public CvInformationResponse updateMyCv(Authentication authentication, @Valid @RequestBody CvInformationUpdateRequest cv) throws JsonProcessingException {
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        return userProfileService.updateCv(cv, currentUser.getAccount().getId());
    }

    @RequestMapping(path = "/cv/current", method = RequestMethod.GET)
    public CvInformationResponse getCv(Authentication authentication){
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        return userProfileService.getCvResponseByUUID(currentUser.getAccount().getId());
    }

    @GetMapping(path = "/current")
    public UserProfileResponse getCurrentUser(Authentication authentication){
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        return userProfileService.findByUUID(currentUser.getAccount().getId());
    }
}
