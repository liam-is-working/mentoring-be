package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.CreateMeetingFeedbackRequest;
import com.example.mentoringapis.models.upStreamModels.MeetingFeedbackResponse;
import com.example.mentoringapis.models.upStreamModels.UpdateMeetingFeedbackRequest;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.service.MeetingFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/meeting-feedback")
@RequiredArgsConstructor
public class MeetingFeedbackController {
    private final MeetingFeedbackService meetingFeedbackService;


    @PostMapping("/{bookingId}")
    public ResponseEntity<MeetingFeedbackResponse.MeetingFeedbackCard> createFeedback(Authentication authentication, @PathVariable Long bookingId, @RequestBody CreateMeetingFeedbackRequest request) throws ResourceNotFoundException {
        var currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
        return ResponseEntity.ok(meetingFeedbackService.createFeedback(request, bookingId, currentUserId));
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<MeetingFeedbackResponse.MeetingFeedbackCard> editFeedback(Authentication authentication, @PathVariable Long feedbackId, @RequestBody UpdateMeetingFeedbackRequest request) throws ResourceNotFoundException, ClientBadRequestError {
        var currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
        return ResponseEntity.ok(meetingFeedbackService.editFeedback(request, currentUserId, feedbackId));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<MeetingFeedbackResponse> getFeedback(@PathVariable UUID userId)
    {return ResponseEntity.ok(meetingFeedbackService.getFeedbacks(userId, null));}

    @GetMapping("/by-booking/{bookingId}")
    public ResponseEntity<MeetingFeedbackResponse> getFeedback(@PathVariable long bookingId)
    {return ResponseEntity.ok(meetingFeedbackService.getFeedbacks(null, bookingId));}

}
