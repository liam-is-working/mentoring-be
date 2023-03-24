package com.example.mentoringapis.controllers;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.errors.FirebaseError;
import com.example.mentoringapis.models.upStreamModels.SignUpWithEmailPasswordRequest;
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
@RequestMapping("sign-up")
@RequiredArgsConstructor
public class SignUpController {

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
    public Mono<ResponseEntity<String>> signUpWithEmailPassword(@Valid @RequestBody SignUpWithEmailPasswordRequest reqBod) throws FirebaseAuthException, FirebaseError {
        return authService.signUpWithEmailAndPassword(reqBod.getEmail(), reqBod.getPassword(), reqBod.getFullName()).map(ResponseEntity::ok);
    }

    @RequestMapping(value = "/email-verification", method = RequestMethod.GET)
    public Mono<ResponseEntity<String>> emailVerify(@RequestParam String oobCode){
        return authService.finishSigningUpWithEmailVerification(oobCode)
                .map(acc -> ResponseEntity.ok(acc.getEmail()));
//        try {
//            var result  = firebaseAuthService.finishSigningUpWithEmailVerification(oobCode);
//            return ResponseEntity.ok(result);
//        }catch (Exc error){
//            return new ResponseEntity<>(error.getErrorMessages(), HttpStatusCode.valueOf(error.getCode()));
//        }
//                .doOnError(FirebaseError.class)
    }
}
