package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ResourceNotFoundException;
import com.example.mentoringapis.service.MailService;
import com.mailjet.client.errors.MailjetException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/mails")
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;

    @GetMapping("/by-seminar/{seminarId}")
    public ResponseEntity sendEmailBySeminar(@PathVariable long seminarId) throws IOException {
        return ResponseEntity.ok(mailService.sendEmail(seminarId, null));
    }

    @GetMapping("/by-mentor/{mentorId}")
    public ResponseEntity sendEmailByMentor(@PathVariable UUID mentorId) {
        CompletableFuture.runAsync(() -> {
            mailService.sendInvitationEmail(mentorId);
        });
        return ResponseEntity.ok().build();
    }
}
