package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.MentoringAuthenticationError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.service.AccountService;
import com.example.mentoringapis.service.AuthService;
import com.example.mentoringapis.utilities.AuthorizationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @RequestMapping(value = "/students", method = RequestMethod.GET)
    public ResponseEntity<List<StudentAccountResponse>> listStudentAccounts() {
        return ResponseEntity.ok(accountService.getStudents());
    }

    @PostMapping(value = "/mentors/invalidate")
    public ResponseEntity<List<UUID>> invalidateMentors(@RequestBody @Valid UuidListRequest uuidListRequest) throws ResourceNotFoundException {
        return ResponseEntity.ok(accountService.invalidateMentors(uuidListRequest.getIds()));
    }

    @DeleteMapping(value = "/{uuid}")
    public ResponseEntity deleteAccount(@PathVariable UUID uuid, Authentication authentication) throws ClientBadRequestError, ResourceNotFoundException, MentoringAuthenticationError {
        var currentUserId = AuthorizationUtils.getCurrentUserUuid(authentication);

        if(accountService.deleteAccount(uuid))
            return ResponseEntity.ok(uuid);
        else
            return ResponseEntity.status(409).build();
    }

}
