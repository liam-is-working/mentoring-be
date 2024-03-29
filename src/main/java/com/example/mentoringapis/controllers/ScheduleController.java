package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.MentoringAuthenticationError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.service.ScheduleService;
import com.example.mentoringapis.utilities.AuthorizationUtils;
import com.example.mentoringapis.utilities.DateTimeUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping()
    public ResponseEntity createSchedule(Authentication authentication, @RequestBody CreateScheduleRequest createScheduleRequest) throws ResourceNotFoundException, ClientBadRequestError, MentoringAuthenticationError {
        createScheduleRequest.validate();
        var currentUser = AuthorizationUtils.getCurrentUser(authentication);
        if(!currentUser.getRole().equalsIgnoreCase("MENTOR")){
            throw new ClientBadRequestError("Only Mentor can have available time");
        }
        scheduleService.createSchedule(currentUser.getAccount().getId(), createScheduleRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{schedule_id}")
    public ResponseEntity editSchedule(Authentication authentication, @RequestBody CreateScheduleRequest createScheduleRequest, @PathVariable long schedule_id) throws ResourceNotFoundException, ClientBadRequestError, MentoringAuthenticationError {
        createScheduleRequest.validate();
        var currentUserId = AuthorizationUtils.getCurrentUserUuid(authentication);
        scheduleService.editSchedule(currentUserId,schedule_id, createScheduleRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create-exception")
    public ResponseEntity createExcDate(Authentication authentication, @RequestBody @Valid CreateExceptionDateRequest request) throws ResourceNotFoundException, ClientBadRequestError, MentoringAuthenticationError {
        request.validate();
        var currentUserId = AuthorizationUtils.getCurrentUserUuid(authentication);
        scheduleService.addExceptionDate(currentUserId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/exception/{excId}")
    public ResponseEntity updateExcDate(Authentication authentication, @RequestBody @Valid UpdateExceptionDateRequest request, @PathVariable long excId) throws ResourceNotFoundException, ClientBadRequestError, MentoringAuthenticationError {
        request.validate();
        var currentUserId = AuthorizationUtils.getCurrentUserUuid(authentication);
        scheduleService.editExceptionDate(currentUserId, excId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity deleteSchedule(Authentication authentication, @PathVariable long scheduleId) throws ResourceNotFoundException, MentoringAuthenticationError {
        var currentUserId = AuthorizationUtils.getCurrentUserUuid(authentication);
        scheduleService.removeSchedule(currentUserId, scheduleId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<ScheduleResponse> get(Authentication authentication,
                                                @RequestParam String startDate,
                                                @RequestParam String endDate) throws ResourceNotFoundException, MentoringAuthenticationError {
        var currentUserId = AuthorizationUtils.getCurrentUserUuid(authentication);
        return ResponseEntity.ok(scheduleService
                .getMentorScheduleBetween(currentUserId, DateTimeUtils.parseStringToLocalDate(startDate), DateTimeUtils.parseStringToLocalDate(endDate), true)
        );
    }

    @GetMapping("/by-mentor/{mentorId}")
    public ResponseEntity<ScheduleResponse> getByMentorId(@RequestParam String startDate,
                                                          @RequestParam String endDate,
                                                          @RequestParam(defaultValue = "false", name = "showBooking") Boolean showBooking,
                                                          @PathVariable UUID mentorId) throws ResourceNotFoundException {
        return ResponseEntity.ok(scheduleService
                .getMentorScheduleBetween(mentorId, DateTimeUtils.parseStringToLocalDate(startDate), DateTimeUtils.parseStringToLocalDate(endDate), showBooking)
        );
    }
}
