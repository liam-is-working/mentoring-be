package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.service.UserProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "user-profile")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/current")
    public UserProfileResponse updateProfile(@Valid @RequestBody UserProfileUpdateRequest request, Authentication authentication){
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        var result = userProfileService.update(request, currentUser.getAccount().getId());
        result.setRole(currentUser.getRole());
        return result;
    }

    @PostMapping(path = "/{mentorId}")
    public MentorAccountResponse updateMentorProfile(@Valid @RequestBody UpdateMentorProfileRequest request,
                                                     @PathVariable("mentorId") UUID mentorId) throws ResourceNotFoundException {
        return userProfileService.update(request, mentorId);
    }

    @RequestMapping(path = "/current/cv", method = RequestMethod.POST)
    public CvInformationResponse updateMyCv(Authentication authentication, @Valid @RequestBody CvInformationUpdateRequest cv) throws JsonProcessingException {
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        return userProfileService.updateCv(cv, currentUser.getAccount().getId());
    }

    @RequestMapping(path = "/current/cv", method = RequestMethod.GET)
    public CvInformationResponse getCv(Authentication authentication){
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        return userProfileService.getCvResponseByUUID(currentUser.getAccount().getId());
    }

    @RequestMapping(path = "/{id}/cv", method = RequestMethod.GET)
    public CvInformationResponse getCv(@PathParam("id") UUID id){
        return userProfileService.getCvResponseByUUID(id);
    }

    @GetMapping(path = "/current")
    public UserProfileResponse getCurrentUser(Authentication authentication){
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        var result = userProfileService.findByUUID(currentUser.getAccount().getId());
        result.setRole(currentUser.getRole());
        return result;
    }
}
