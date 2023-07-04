package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.CreateScheduleRequest;
import com.example.mentoringapis.models.upStreamModels.DeleteScheduleRequest;
import com.example.mentoringapis.models.upStreamModels.ScheduleResponse;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.service.ScheduleService;
import com.example.mentoringapis.utilities.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping("/create")
    public ResponseEntity createSchedule(Authentication authentication, @RequestBody CreateScheduleRequest createScheduleRequest) throws ResourceNotFoundException {
        var currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
        return ResponseEntity.ok(scheduleService.createSchedule(currentUserId, createScheduleRequest));
    }

    @PostMapping("/delete")
    public ResponseEntity delete(Authentication authentication, @RequestBody DeleteScheduleRequest deleteScheduleRequest) throws ResourceNotFoundException {
        var currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
        scheduleService.removeSchedule(currentUserId, deleteScheduleRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<ScheduleResponse> get(Authentication authentication,
                                                @RequestParam String startDate,
                                                @RequestParam String endDate) throws ResourceNotFoundException {
        var currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
        return ResponseEntity.ok(scheduleService
                .getMentorScheduleBetween(currentUserId, DateTimeUtils.parseDate(startDate), DateTimeUtils.parseDate(endDate))
        );
    }
}
