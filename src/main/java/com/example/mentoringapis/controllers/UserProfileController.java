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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "user-profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping(path = "/current")
    public UserProfileResponse updateProfile(@Valid @RequestBody UserProfileUpdateRequest request, Authentication authentication){
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        var result = userProfileService.update(request, currentUser.getAccount().getId());
        result.setRole(currentUser.getRole());
        return result;
    }

    @PostMapping(path = "mentors/{mentorId}")
    public MentorAccountResponse updateMentorProfile(@Valid @RequestBody UpdateMentorProfileRequest request,
                                                     @PathVariable("mentorId") UUID mentorId) throws ResourceNotFoundException {
        return userProfileService.updateMentor(request, mentorId);
    }

    @PostMapping(path = "staffs/{staffId}")
    public StaffAccountResponse updateStaffProfile(@Valid @RequestBody UpdateMentorProfileRequest request,
                                                     @PathVariable("staffId") UUID staffId) throws ResourceNotFoundException {
        return userProfileService.updateStaff(request, staffId);
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

    @GetMapping(path = "/{id}")
    public UserProfileResponse getUserById(@PathVariable UUID id){
        return userProfileService.findByUUID(id);
    }


    @GetMapping(path = "/mentees")
    public List<UserProfileResponse> getAllMenteeWithEmail(@RequestParam(value = "email", defaultValue = "") String email){
            return userProfileService.getAllMenteeByEmail(email);
    }

    @GetMapping("/{profileId}/followers")
    public ResponseEntity<List<UserProfileResponse>> getFollower(@PathVariable UUID profileId) throws ResourceNotFoundException {
            return ResponseEntity.ok(userProfileService.getFollowers(profileId));
    }

    @GetMapping("/{profileId}/followings")
    public ResponseEntity<List<UserProfileResponse>> getFollowing(@PathVariable UUID profileId) throws ResourceNotFoundException {
        return ResponseEntity.ok(userProfileService.getFollowing(profileId));
    }


    @PostMapping("/follow")
    public ResponseEntity follow(Authentication authentication, @RequestParam(value = "mentor") UUID mentorId){
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        CompletableFuture.runAsync(() -> userProfileService.follow(mentorId, currentUser.getAccount().getId()));
//        userProfileService.follow(mentorId, currentUser.getAccount().getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unfollow")
    public ResponseEntity unfollow(Authentication authentication, @RequestParam(value = "mentor") UUID mentorId){
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        CompletableFuture.runAsync(() -> userProfileService.unfollow(mentorId, currentUser.getAccount().getId()));
//        userProfileService.follow(mentorId, currentUser.getAccount().getId());
        return ResponseEntity.ok().build();
    }

    @RequestMapping(path = "/{id}/cv", method = RequestMethod.GET)
    public CvInformationResponse getCv(@PathVariable UUID id){
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
