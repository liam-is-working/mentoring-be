package com.example.mentoringapis.controllers;

import com.example.mentoringapis.entities.TopicCategory;
import com.example.mentoringapis.entities.TopicField;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.MentoringAuthenticationError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.AccountResponse;
import com.example.mentoringapis.models.upStreamModels.AccountUpdateRequest;
import com.example.mentoringapis.models.upStreamModels.CreateStaffAccountRequest;
import com.example.mentoringapis.models.upStreamModels.TopicDetailResponse;
import com.example.mentoringapis.repositories.TopicCategoryRepository;
import com.example.mentoringapis.repositories.TopicFieldRepository;
import com.example.mentoringapis.service.AccountService;
import com.example.mentoringapis.service.AuthService;
import com.example.mentoringapis.service.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AccountService accountService;
    private final TopicFieldRepository topicFieldRepository;
    private final TopicCategoryRepository topicCategoryRepository;
    private final TopicService topicService;
    private final AuthService authService;

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> getAccounts(){
        return ResponseEntity.ok(accountService.getAll());
    }

    @GetMapping("/topics")
    public ResponseEntity<List<TopicDetailResponse>> getTopics(){
        return ResponseEntity.ok(topicService.getAll());
    }

    @GetMapping("/topic-fields")
    public ResponseEntity<Iterable<TopicField>> getTopicFields(){
        return ResponseEntity.ok(topicFieldRepository.findAll());
    }

    @GetMapping("/topic-categories")
    public ResponseEntity<Iterable<TopicCategory>> getTopicCategories(){
        return ResponseEntity.ok(topicCategoryRepository.findAll());
    }

    @PostMapping(value = "/staffs")
    public ResponseEntity<String> createStaffAccount(@Valid @RequestBody CreateStaffAccountRequest request) throws MentoringAuthenticationError, ClientBadRequestError {
        return ResponseEntity.ok(authService.createStaffAccount(request));
    }

    @PostMapping("/accounts/{accountId}")
    public ResponseEntity<List<AccountResponse>> updateAccount( @PathVariable UUID accountId, @Valid @RequestBody AccountUpdateRequest accountUpdateRequest) throws ResourceNotFoundException {
        return ResponseEntity.ok(accountService.updateStatus(accountId, accountUpdateRequest));
    }

}
