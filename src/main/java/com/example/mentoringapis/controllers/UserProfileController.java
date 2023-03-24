package com.example.mentoringapis.controllers;

import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.models.upStreamModels.UserProfileUpdateRequest;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "user-profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping
    public UserProfile updateProfile(@RequestBody UserProfileUpdateRequest request, Authentication authentication){
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        return userProfileService.update(request, currentUser.getAccount().getId());
    }
}
