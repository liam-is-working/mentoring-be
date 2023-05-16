package com.example.mentoringapis.controllers;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.errors.MentoringAuthenticationError;
import com.example.mentoringapis.models.upStreamModels.CreateMentorAccountRequest;
import com.example.mentoringapis.models.upStreamModels.CreateStaffAccountRequest;
import com.example.mentoringapis.repositories.AccountsRepository;
import com.example.mentoringapis.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AuthService authService;
    private final AccountsRepository accountsRepository;

    @RequestMapping(value = "/mentors", method = RequestMethod.POST)
    public ResponseEntity<String> createMentorAccount(@Valid @RequestBody CreateMentorAccountRequest request) throws MentoringAuthenticationError {
        return ResponseEntity.ok(authService.createMentorAccount(request));
    }

    @RequestMapping(value = "/mentors", method = RequestMethod.GET)
    public ResponseEntity<List<Account>> listMentorAccounts() {
        return ResponseEntity.ok(accountsRepository.findAccountsByRole(Account.Role.MENTOR.name()));
    }

    @RequestMapping(value = "/staffs", method = RequestMethod.POST)
    public ResponseEntity<String> createStaffAccount(@Valid @RequestBody CreateStaffAccountRequest request) throws MentoringAuthenticationError {
        return ResponseEntity.ok(authService.createStaffAccount(request));
    }

    @RequestMapping(value = "/staffs", method = RequestMethod.GET)
    public ResponseEntity<List<Account>> listStaffAccounts() {
        return ResponseEntity.ok(accountsRepository.findAccountsByRole(Account.Role.STAFF.name()));
    }

}
