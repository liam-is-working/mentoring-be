package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.models.upStreamModels.SeminarFeedbackReportResponse;
import com.example.mentoringapis.models.upStreamModels.SeminarFeedbackRequest;
import com.example.mentoringapis.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService feedbackService;

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
}
