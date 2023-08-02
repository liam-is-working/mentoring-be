package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.SeminarFeedbackReportResponse;
import com.example.mentoringapis.models.upStreamModels.SeminarFeedbackRequest;
import com.example.mentoringapis.service.FeedbackService;
import com.example.mentoringapis.service.MailService;
import com.example.mentoringapis.utilities.AuthorizationUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService feedbackService;
    private final MailService mailService;

    @GetMapping("/seminar/{seminarId}")
    public ResponseEntity<Object> getFeedbackFormForSeminar(@PathVariable Long seminarId) throws IOException, ResourceNotFoundException {
        return ResponseEntity.ok(feedbackService.getFeedbackForm(seminarId));
    }
    @PostMapping("/seminar/{seminarId}")
    public ResponseEntity getFeedbackFormForSeminar(@PathVariable Long seminarId, @RequestBody @Valid SeminarFeedbackRequest feedbackResult) throws IOException, ResourceNotFoundException, ClientBadRequestError {
        feedbackService.updateFeedback(seminarId, feedbackResult);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/seminar-feedback-report/{seminarId}")
    public Mono<ResponseEntity<SeminarFeedbackReportResponse>> getFeedbackReportForSeminar(@PathVariable Long seminarId) throws IOException, ResourceNotFoundException {
        return feedbackService.getFeedbackReport(seminarId)
                .map(ResponseEntity::ok);
    }

    @Data
    private static class MentorIdsRequest{
        @NotNull
        private List<UUID> mentorIds;
    }
}
