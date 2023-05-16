package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Gender;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.errors.FirebaseError;
import com.example.mentoringapis.errors.MentoringAuthenticationError;
import com.example.mentoringapis.models.downStreamModels.FirebaseBaseResponse;
import com.example.mentoringapis.models.downStreamModels.FirebaseErrorResponse;
import com.example.mentoringapis.models.upStreamModels.CreateMentorAccountRequest;
import com.example.mentoringapis.models.upStreamModels.CreateStaffAccountRequest;
import com.example.mentoringapis.models.upStreamModels.SignInRes;
import com.example.mentoringapis.models.upStreamModels.SignInWithGoogleRequest;
import com.example.mentoringapis.repositories.AccountsRepository;
import com.example.mentoringapis.repositories.FirebaseAuthRepository;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.security.JwtTokenProvider;
import com.example.mentoringapis.validation.ValidatorUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    public Mono<String> sendPasswordResetEmail(String email) throws FirebaseError {
        if (accountsRepository.findByEmail(email).isEmpty())
            throw FirebaseError.builder()
                    .code(HttpStatus.CONFLICT.value())
                    .errorMessages(List.of("Cannot find account in db with email: " + email))
                    .build();
        return firebaseAuthRepo.sendResetPasswordEmail(email)
                .handle((sendEmailVerificationResponse, stringSynchronousSink) -> {
                    if (sendEmailVerificationResponse.getError() != null) {
                        stringSynchronousSink.error(handleError(sendEmailVerificationResponse));
                    } else {
                        stringSynchronousSink.next(sendEmailVerificationResponse.getEmail());
                    }
                });

    }

    public Mono<String> applyPasswordChange(String oobCode, String newPassword) {
        return firebaseAuthRepo.verifyPasswordChangeEmail(oobCode, newPassword)
                .handle((passwordChangeRes, stringSynchronousSink) -> {
                    if (passwordChangeRes.getError() != null) {
                        stringSynchronousSink.error(handleError(passwordChangeRes));
                    } else {
                        stringSynchronousSink.next(passwordChangeRes.getEmail());
                    }
                });
    }

    public Mono<Account> finishSigningUpWithEmailVerification(String oob) {
        return firebaseAuthRepo.verifyEmail(oob)
                .handle(((emailVerificationResponse, synchronousSink) -> {
                    if (emailVerificationResponse.getError() != null) {
                        synchronousSink.error(handleError(emailVerificationResponse));
                    } else {
                        //create new account and user profile of the account
                        try {
                            var userRecord = getUserRecord(emailVerificationResponse.getEmail());
                            var newAccount = createNewAccountAndProfile(emailVerificationResponse.getEmail(),
                                    emailVerificationResponse.getLocalId(), userRecord.getDisplayName(), userRecord.getPhotoUrl(), Account.Role.STUDENT, true);

                            synchronousSink.next(newAccount);
                        } catch (Exception ex) {
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

    public SignInRes signInWithGoogle(SignInWithGoogleRequest request) throws FirebaseAuthException, FirebaseError, MentoringAuthenticationError {
        //TODO check if token is valid
        //check if user is exist and authenticated with Google
        var userRecord = firebaseAuth.getUser(request.getLocalId());
        if (userRecord == null || !request.getEmail().equals(userRecord.getEmail()))
            throw FirebaseError.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .errorMessages(List.of("User does not exist on Firebase or has not been authenticated with Google with email: " + request.getEmail()))
                    .build();

        var isFptStudentEmail = ValidatorUtils.isFptStudentEMail(request.getEmail());

        var accountOptional = accountsRepository.findByEmail(request.getEmail());

        //email not in FPT group and not in db
        if (!isFptStudentEmail && accountOptional.isEmpty()) {
            throw MentoringAuthenticationError.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .errorMessages(String.format("Email: %s. Attempt to create account outside of FPT mail group without permission", request.getEmail()))
                    .build();
        }

        //create account to db if not exist
        return accountOptional
                .map(account -> {
                    if (!isFptStudentEmail && !account.isAuthenticated()) {
                        account.setAuthenticated(true);
                        accountsRepository.save(account);
                    }
                    return SignInRes.buildFromAccount(account, jwtTokenProvider);
                })
                .orElseGet(() -> {
                    //create new STUDENT account in db
                    var newAccount = createNewAccountAndProfile(request.getEmail(), request.getLocalId()
                            , request.getFullName(), request.getAvatarUrl(), Account.Role.STUDENT, true);
                    return SignInRes.buildFromAccount(newAccount, jwtTokenProvider);
                });
    }

    private Account createNewAccountAndProfile(String email, String firebaseId,
                                               String fullName, String avatarUrl, Account.Role role, boolean isAuthenticated) {
        var newAccount = new Account(email,
                firebaseId, role, isAuthenticated);
        var newProfile = new UserProfile();
        newProfile.setGender(Gender.others);
        newProfile.setFullName(fullName);
        newAccount.setUserProfile(newProfile);
        newProfile.setAccount(newAccount);
        newProfile.setAvatarUrl(avatarUrl);

        return accountsRepository.save(newAccount);
    }

    public String createMentorAccount(CreateMentorAccountRequest request) throws MentoringAuthenticationError {
        if(ValidatorUtils.isFptStudentEMail(request.getEmail()))
            throw MentoringAuthenticationError.builder()
                    .httpStatus(HttpStatus.CONFLICT)
                    .errorMessages(String.format("Email: %s is an FPT Student email", request.getEmail()))
                    .build();
        if(accountsRepository.findByEmail(request.getEmail()).isPresent())
            throw MentoringAuthenticationError.builder()
            .httpStatus(HttpStatus.CONFLICT)
            .errorMessages(String.format("Email: %s already exists", request.getEmail()))
            .build();
        var newMentorAccount = createNewAccountAndProfile(request.getEmail(), null
                , request.getFullName(), request.getAvatarUrl(), Account.Role.MENTOR, false);
        return newMentorAccount.getEmail();
    }

    public String createStaffAccount(CreateStaffAccountRequest request) throws MentoringAuthenticationError {
        if(ValidatorUtils.isFptStudentEMail(request.getEmail()))
            throw MentoringAuthenticationError.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .errorMessages(String.format("Email: %s is an FPT Student email", request.getEmail()))
                    .build();
        if(accountsRepository.findByEmail(request.getEmail()).isPresent())
            throw MentoringAuthenticationError.builder()
                    .httpStatus(HttpStatus.CONFLICT)
                    .errorMessages(String.format("Email: %s already exists", request.getEmail()))
                    .build();
        var newStaffAccount = createNewAccountAndProfile(request.getEmail(), null
                , request.getFullName(), request.getAvatarUrl(), Account.Role.STAFF, false);
        return newStaffAccount.getEmail();
    }


}
