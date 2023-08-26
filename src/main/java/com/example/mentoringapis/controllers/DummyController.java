package com.example.mentoringapis.controllers;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.MenteeMentorId;
import com.example.mentoringapis.errors.FirebaseError;
import com.example.mentoringapis.models.upStreamModels.MentorListResponse;
import com.example.mentoringapis.repositories.AccountsRepository;
import com.example.mentoringapis.repositories.UserProfileRepository;
import com.example.mentoringapis.service.*;
import com.google.cloud.bigquery.*;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController("api/dummy")
@RequiredArgsConstructor
public class DummyController {

    private final AuthService authService;
    private final MentorMenteeRatingService mentorMenteeRatingService;
    private final MailService mailService;
    private final AccountsRepository accountsRepository;
    private final UserProfileService userProfileService;

    @GetMapping(value = "helloWorld")
    public String helloWorld(Authentication authentication){

        return "Hello world!" + authentication.getName();
    }

    @GetMapping(value = "test")
    public UserRecord test() throws FirebaseError {
        return authService.getUserRecord("vulam2704012312@gmail.com");
    }

    @GetMapping(value = "testAdd")
    public Account testAdd() {
        var newAccount =  new Account();
        newAccount.setEmail("example123@gmail.com");
        return accountsRepository.save(newAccount);
    }


    @GetMapping("/test1")
    public ResponseEntity test1(){
        return ResponseEntity.ok(mentorMenteeRatingService.getAllCombination());
    }

    @GetMapping("/test2")
    public ResponseEntity test2(){
        mentorMenteeRatingService.accumulateRating();
        return ResponseEntity.ok().build();
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<List<MentorListResponse.MentorCard>> test3(@PathVariable UUID id) throws InterruptedException {
//        var result = userProfileService.getRecommendation(id);
//        return ResponseEntity.ok(result);
//    };


    @GetMapping()
    public void verifyEmailEndpoint(String mode, String oobCode){

    }
}
