package com.example.mentoringapis.controllers;

import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.FirebaseError;
import com.example.mentoringapis.errors.MentoringAuthenticationError;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("sign-in")
@RequiredArgsConstructor
public class SignInController {

    private final AuthService authService;

    @RequestMapping(value = "/with-google", method = RequestMethod.POST)
    public ResponseEntity<SignInRes> signInWithGoogle(@Valid @RequestBody SignInWithGoogleRequest request) throws FirebaseAuthException, FirebaseError, MentoringAuthenticationError, ClientBadRequestError {
        return ResponseEntity.ok(authService.signInWithGoogle(request));
    }

}