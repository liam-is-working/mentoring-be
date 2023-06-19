package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.CreateTopicRequest;
import com.example.mentoringapis.models.upStreamModels.TopicDetailResponse;
import com.example.mentoringapis.models.upStreamModels.UpdateTopicRequest;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @PostMapping()
    public ResponseEntity<TopicDetailResponse> createTopic(@RequestBody CreateTopicRequest topicRequest, Authentication authentication) throws ResourceNotFoundException {
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(topicService.createTopic(topicRequest, currentUser.getAccount().getId()));
    }

    @PostMapping("/{topicId}")
    public ResponseEntity<TopicDetailResponse> editTopic(@RequestBody UpdateTopicRequest topicRequest, Authentication authentication, @PathVariable Long topicId) throws ResourceNotFoundException {
        var currentUser = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(topicService.editTopic(topicRequest, currentUser.getAccount().getId(), topicId));
    }
}
