package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.MentoringAuthenticationError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.CreateTopicRequest;
import com.example.mentoringapis.models.upStreamModels.TopicDetailResponse;
import com.example.mentoringapis.models.upStreamModels.UpdateTopicRequest;
import com.example.mentoringapis.service.TopicService;
import com.example.mentoringapis.utilities.AuthorizationUtils;
import com.example.mentoringapis.validation.EnumField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/by-mentor/{mentorId}")
    public ResponseEntity getTopicByMentor(@PathVariable UUID mentorId){
        return ResponseEntity.ok(topicService.getByMentorId(mentorId));
    }

    @PostMapping()
    public ResponseEntity<TopicDetailResponse> createTopic(@RequestBody CreateTopicRequest topicRequest, Authentication authentication) throws ResourceNotFoundException, MentoringAuthenticationError {
        var currentUuid = AuthorizationUtils.getCurrentUserUuid(authentication);
        return ResponseEntity.ok(topicService.createTopic(topicRequest, currentUuid));
    }

    @PostMapping("/{topicId}")
    public ResponseEntity<TopicDetailResponse> editTopic(@RequestBody UpdateTopicRequest topicRequest, Authentication authentication, @PathVariable Long topicId) throws ResourceNotFoundException, MentoringAuthenticationError {
        var currentUuid = AuthorizationUtils.getCurrentUserUuid(authentication);
        return ResponseEntity.ok(topicService.editTopic(topicRequest, currentUuid, topicId));
    }

    @PostMapping("/update-status")
    public ResponseEntity<List<TopicDetailResponse>> changingStatus(@RequestBody @Valid StatusChangingRequest request) throws ClientBadRequestError {
        return ResponseEntity.ok(topicService.changeStatus(request.getIds(), request.getStatus()));
    }


    @Data
    private static class StatusChangingRequest {
        @NotNull
        @NotEmpty
        List<Long> ids;
        @EnumField(availableValues = { "WAITING", "DELETED", "ACCEPTED", "REJECTED", "ARCHIVED"},
                message = "enum: WAITING, DELETED, ACCEPTED, REJECTED, ARCHIVED")
        @NotEmpty
        String status;
    }
}
