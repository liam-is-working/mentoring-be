package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.MentoringAuthenticationError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.CreateMentorAccountRequest;
import com.example.mentoringapis.models.upStreamModels.MentorAccountResponse;
import com.example.mentoringapis.models.upStreamModels.StaffAccountResponse;
import com.example.mentoringapis.models.upStreamModels.UuidListRequest;
import com.example.mentoringapis.service.AccountService;
import com.example.mentoringapis.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AuthService authService;
    private final AccountService accountService;

    @RequestMapping(value = "/mentors", method = RequestMethod.POST)
    public ResponseEntity<MentorAccountResponse> createMentorAccount(@Valid @RequestBody CreateMentorAccountRequest request) throws MentoringAuthenticationError {
        return ResponseEntity.ok(authService.createMentorAccount(request));
    }

    @RequestMapping(value = "/mentors", method = RequestMethod.GET)
    public ResponseEntity<List<MentorAccountResponse>> listMentorAccounts() {
        return ResponseEntity.ok(accountService.getMentors());
    }

    @RequestMapping(value = "/staffs", method = RequestMethod.GET)
    public ResponseEntity<List<StaffAccountResponse>> listStaffAccounts() {
        return ResponseEntity.ok(accountService.getStaffs());
    }

    @PostMapping(value = "/mentors/invalidate")
    public ResponseEntity<List<UUID>> invalidateMentors(@RequestBody @Valid UuidListRequest uuidListRequest) throws ResourceNotFoundException {
        return ResponseEntity.ok(accountService.invalidateMentors(uuidListRequest.getIds()));
    }

}
