package com.example.mentoringapis.controllers;

import com.example.mentoringapis.entities.TopicCategory;
import com.example.mentoringapis.entities.TopicField;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.MentoringAuthenticationError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.repositories.TopicCategoryRepository;
import com.example.mentoringapis.repositories.TopicFieldRepository;
import com.example.mentoringapis.service.*;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
    private final BookingService bookingService;
    private final TopicFieldCategoryService topicFieldCategoryService;
    private final AuthService authService;
    private final AppConfigService appConfigService;

    @GetMapping("/configs")
    public ResponseEntity<List<AppConfigResponse>> getConfigs(){
        return ResponseEntity.ok(appConfigService.getAll());
    }

    @PostMapping("/configs")
    public ResponseEntity<AppConfigResponse> createNewConfig(@RequestBody AppConfigRequest appConfigRequest){
        return ResponseEntity.ok(appConfigService.createNewAppConfig(appConfigRequest));
    }

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

    @PostMapping("/topic-fields")
    public ResponseEntity<Iterable<TopicField>> createTopicFields(@RequestBody CreateSimpleEntityRequest request) throws ClientBadRequestError {
        return ResponseEntity.ok(topicFieldCategoryService.createTopicField(request));
    }

    @PostMapping("/topic-categories")
    public ResponseEntity<Iterable<TopicCategory>> createTopicCategories(@RequestBody CreateSimpleEntityRequest request) throws ClientBadRequestError {
        return ResponseEntity.ok(topicFieldCategoryService.createTopicCategory(request));
    }

    @PutMapping("/topic-fields/{id}")
    public ResponseEntity<Iterable<TopicField>> editTopicFields(@RequestBody CreateSimpleEntityRequest request, @PathVariable long id) throws ClientBadRequestError {
        return ResponseEntity.ok(topicFieldCategoryService.editTopicField(request, id));
    }

    @PutMapping("/topic-categories/{id}")
    public ResponseEntity<Iterable<TopicCategory>> editTopicCategories(@RequestBody CreateSimpleEntityRequest request, @PathVariable long id) throws ClientBadRequestError {
        return ResponseEntity.ok(topicFieldCategoryService.editTopicCats(request, id));
    }

    @DeleteMapping("/topic-fields/{id}")
    public ResponseEntity<Iterable<TopicField>> deleteTopicField(@PathVariable long id) throws ResourceNotFoundException {
        return ResponseEntity.ok(topicFieldCategoryService.deleteField(id));
    }

    @DeleteMapping("/topic-categories/{id}")
    public ResponseEntity<Iterable<TopicCategory>> deleteTopicCats(@PathVariable long id) throws ResourceNotFoundException {
        return ResponseEntity.ok(topicFieldCategoryService.deleteCat(id));
    }

    @GetMapping("/bookings")
    public ResponseEntity<BookingListResponse> getBookings(@RequestParam(required = false, defaultValue = "") String topicName){
        return ResponseEntity.ok(bookingService.getAllBooking(topicName));
    }

    @PostMapping(value = "/staffs")
    public ResponseEntity<String> createStaffAccount(@Valid @RequestBody CreateStaffAccountRequest request) throws MentoringAuthenticationError, ClientBadRequestError {
        return ResponseEntity.ok(authService.createStaffAccount(request));
    }

    @PostMapping("/accounts/{accountId}")
    public ResponseEntity<List<AccountResponse>> updateAccount( @PathVariable UUID accountId, @Valid @RequestBody AccountUpdateRequest accountUpdateRequest) throws ResourceNotFoundException {
        return ResponseEntity.ok(accountService.updateStatus(accountId, accountUpdateRequest));
    }

    @Getter
    @Setter
    public static class CreateSimpleEntityRequest{
        private String name;
    }

}
