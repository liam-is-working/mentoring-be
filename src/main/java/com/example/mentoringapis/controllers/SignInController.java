package com.example.mentoringapis.controllers;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.errors.FirebaseError;
import com.example.mentoringapis.models.upStreamModels.SignInRes;
import com.example.mentoringapis.models.upStreamModels.SignInWithGoogleRequest;
import com.example.mentoringapis.models.upStreamModels.SignInWithPasswordRequest;
import com.example.mentoringapis.security.JwtTokenProvider;
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

    @ExceptionHandler(FirebaseError.class)
    public ResponseEntity<Object> handleFirebaseException(FirebaseError ex){
        return new ResponseEntity<>(ex.getErrorMessages(), HttpStatusCode.valueOf(ex.getCode()));
    }

    @ExceptionHandler(FirebaseAuthException.class)
    public ResponseEntity<Object> handleFirebaseAuthException(FirebaseAuthException ex){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/with-password", method = RequestMethod.POST)
    public Mono<ResponseEntity<SignInRes>> signInWithPassword(@Valid @RequestBody SignInWithPasswordRequest request){
        return authService.signInWithEmailAndPassword(request.getEmail(), request.getPassword())
                .map(ResponseEntity::ok);
    }

    @RequestMapping(value = "/with-google", method = RequestMethod.POST)
    public ResponseEntity<SignInRes> signInWithGoogle(@Valid @RequestBody SignInWithGoogleRequest request) throws FirebaseAuthException, FirebaseError {
        return ResponseEntity.ok(authService.signInWithGoogle(request));
    }

}
