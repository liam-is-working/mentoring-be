package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.service.ScheduleService;
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
    public ResponseEntity createSchedule(Authentication authentication, @RequestBody CreateScheduleRequest createScheduleRequest) throws ResourceNotFoundException, ClientBadRequestError {
        createScheduleRequest.validate();
        var currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
        scheduleService.createSchedule(currentUserId, createScheduleRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{schedule_id}")
    public ResponseEntity editSchedule(Authentication authentication, @RequestBody CreateScheduleRequest createScheduleRequest, @PathVariable long schedule_id) throws ResourceNotFoundException, ClientBadRequestError {
        var currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
        scheduleService.editSchedule(currentUserId,schedule_id, createScheduleRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create-exception")
    public ResponseEntity createExcDate(Authentication authentication, @RequestBody @Valid CreateExceptionDateRequest request) throws ResourceNotFoundException, ClientBadRequestError {
        var currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
        scheduleService.addExceptionDate(currentUserId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/exception/{excId}")
    public ResponseEntity updateExcDate(Authentication authentication, @RequestBody @Valid UpdateExceptionDateRequest request, @PathVariable long excId) throws ResourceNotFoundException, ClientBadRequestError {
        var currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
        scheduleService.editExceptionDate(currentUserId, excId, request);
        return ResponseEntity.ok().build();
    }

//    @PostMapping("/delete")
//    public ResponseEntity delete(Authentication authentication, @RequestBody DeleteScheduleRequest deleteScheduleRequest) throws ResourceNotFoundException {
//        var currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
//        scheduleService.removeSchedule(currentUserId, deleteScheduleRequest);
//        return ResponseEntity.ok().build();
//    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity deleteSchedule(Authentication authentication, @PathVariable long scheduleId) throws ResourceNotFoundException {
        var currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
        scheduleService.removeSchedule(currentUserId, scheduleId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<ScheduleResponse> get(Authentication authentication,
                                                @RequestParam String startDate,
                                                @RequestParam String endDate) throws ResourceNotFoundException {
        var currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
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
