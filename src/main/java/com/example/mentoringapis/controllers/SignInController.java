package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.FirebaseError;
import com.example.mentoringapis.errors.MentoringAuthenticationError;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("sign-in")
@RequiredArgsConstructor
public class SignInController {

    private final AuthService authService;

    @RequestMapping(value = "/with-password", method = RequestMethod.POST)
    public Mono<ResponseEntity<SignInRes>> signInWithPassword(@Valid @RequestBody SignInWithPasswordRequest request) {
        return authService.signInWithEmailAndPassword(request.getEmail(), request.getPassword())
                .map(ResponseEntity::ok);
    }

    @RequestMapping(value = "/with-google", method = RequestMethod.POST)
    public ResponseEntity<SignInRes> signInWithGoogle(@Valid @RequestBody SignInWithGoogleRequest request) throws FirebaseAuthException, FirebaseError, MentoringAuthenticationError {
        return ResponseEntity.ok(authService.signInWithGoogle(request));
    }

    @RequestMapping(value = "/send-reset-email", method = RequestMethod.POST)
    public Mono<ResponseEntity<String>> signInWithGoogle(@Valid @RequestBody SendMailResetPasswordRequest request) throws FirebaseError {
        return authService.sendPasswordResetEmail(request.getEmail())
                .map(ResponseEntity::ok);
    }

    @RequestMapping(value = "/apply-password-change", method = RequestMethod.POST)
    public Mono<ResponseEntity<String>> signInWithGoogle(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.applyPasswordChange(request.getOobCode(), request.getPassword())
                .map(ResponseEntity::ok);
    }

}