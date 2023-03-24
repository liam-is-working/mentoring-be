package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.errors.FirebaseError;
import com.example.mentoringapis.models.downStreamModels.FirebaseBaseResponse;
import com.example.mentoringapis.models.downStreamModels.FirebaseErrorResponse;
import com.example.mentoringapis.models.upStreamModels.SignInRes;
import com.example.mentoringapis.models.upStreamModels.SignInWithGoogleRequest;
import com.example.mentoringapis.repositories.AccountsRepository;
import com.example.mentoringapis.repositories.FirebaseAuthRepository;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.security.JwtTokenProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final FirebaseAuthRepository firebaseAuthRepo;
    private final AccountsRepository accountsRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final FirebaseAuth firebaseAuth;

    private FirebaseError handleError(FirebaseBaseResponse response) {
        return FirebaseError.builder()
                .code(HttpStatus.CONFLICT.value())
                .errorMessages(
                        response.getError().getErrors().stream()
                                .map(FirebaseErrorResponse.ErrorDetail::getMessage)
                                .collect(Collectors.toList()))
                .build();
    }

    public CustomUserDetails getUserDetailsByUuid(UUID uuid) {
        var accountInfo = accountsRepository.findById(uuid);
        return accountInfo.map(CustomUserDetails::new).orElse(null);
    }

    public UserRecord getUserRecord(String email) {
        try {
            return firebaseAuth.getUserByEmail(email);
        } catch (FirebaseAuthException e) {
            //add log here
            return null;
            //TODO handle null
        }
    }

    public Mono<Account> finishSigningUpWithEmailVerification(String oob) {
        return firebaseAuthRepo.verifyEmail(oob)
                .handle(((emailVerificationResponse, synchronousSink) -> {
                    if (emailVerificationResponse.getError() != null) {
                        synchronousSink.error(handleError(emailVerificationResponse));
                    } else {
                        //create new account and user profile of the account
                        try{
                            var userRecord = getUserRecord(emailVerificationResponse.getEmail());
                            var newAccount = createNewAccountAndProfile(emailVerificationResponse.getEmail(),
                                    emailVerificationResponse.getLocalId(), userRecord.getDisplayName());

                            synchronousSink.next(newAccount);
                        }catch (Exception ex){
                            try {
                                firebaseAuth.deleteUser(emailVerificationResponse.getLocalId());
                            } catch (FirebaseAuthException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }));
    }

    public Mono<SignInRes> signInWithEmailAndPassword(String email, String password) {
        return firebaseAuthRepo.signInWithEmailAndPassword(email, password)
                .flatMap(res -> {
                    if (res.getError() != null) {
                        return Mono.error(handleError(res));
                    }

                    var account = accountsRepository.findByEmail(email);

                    return account
                            .map(Mono::just)
                            .orElse(Mono.error(FirebaseError.builder()
                                    .code(HttpStatus.NOT_FOUND.value())
                                    .errorMessages(List.of("Cant find user in db with email:" + email))
                                    .build())
                            );

                }).map(account -> SignInRes.buildFromAccount(account, jwtTokenProvider));
    }

    public Mono<String> signUpWithEmailAndPassword(String email, String password, String fullName) throws FirebaseAuthException {
        var userRecord = getUserRecord(email);
        if (userRecord != null && !userRecord.isEmailVerified()) {
            //Delete user if email has not been verified
            firebaseAuth.deleteUser(userRecord.getUid());
        } else if (userRecord != null && userRecord.isEmailVerified()) {
            //Email has been used
            return Mono.error(FirebaseError.builder().code(HttpStatus.CONFLICT.value())
                    .errorMessages(List.of("Email has been used")).build());
        }

        //create user and sent email
        return firebaseAuthRepo.signUpByEmailPassword(email, password)
                .flatMap(signUpRes -> {
                    //create user fail
                    if (signUpRes.getError() != null) {
                        return Mono.error(handleError(signUpRes));
                    }

                    UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(signUpRes.getLocalId());
                    request.setDisplayName(fullName);
                    try {
                        firebaseAuth.updateUser(request);
                    } catch (FirebaseAuthException e) {
                        return Mono.error(FirebaseError.builder()
                                .code(HttpStatus.CONFLICT.value())
                                .errorMessages(List.of("Cannot update user name in firebase")).build());
                    }

                    //send email verification
                    return firebaseAuthRepo.sendEmailVerification(signUpRes.getIdToken());
                })
                .flatMap(sendEmailVerificationRes -> {
                    //send email error
                    if (sendEmailVerificationRes.getError() != null) {
                        return Mono.error(handleError(sendEmailVerificationRes));
                    }

                    //send email successfully
                    return Mono.just(sendEmailVerificationRes.getEmail());
                });
    }

    public SignInRes signInWithGoogle(SignInWithGoogleRequest request) throws FirebaseAuthException, FirebaseError {
        //TODO check if token is valid
        //check if user is exist
        var userRecord = firebaseAuth.getUser(request.getLocalId());
        if (userRecord == null || !request.getEmail().equals(userRecord.getEmail()))
            throw FirebaseError.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .errorMessages(List.of("Cant find user with email: " + request.getEmail()))
                    .build();

        //create account to db if not exist
        return accountsRepository.findByEmail(request.getEmail())
                .map(account -> SignInRes.buildFromAccount(account, jwtTokenProvider))
                .orElseGet(() -> {
                    //create account in db
                    var newAccount = createNewAccountAndProfile(request.getEmail(), request.getLocalId(), request.getFullName());
                    return SignInRes.buildFromAccount(newAccount, jwtTokenProvider);
                });
    }

    private Account createNewAccountAndProfile(String email, String firebaseId, String fullName) {
        var newAccount = new Account(email,
                firebaseId, false);
        var newProfile = new UserProfile();
        newProfile.setGender("Unknown");
        newProfile.setFullName(fullName);
        newAccount.setUserProfile(newProfile);
        newProfile.setAccount(newAccount);
        return accountsRepository.save(newAccount);
    }
}
