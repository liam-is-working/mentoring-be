package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.Department;
import com.example.mentoringapis.entities.Gender;
import com.example.mentoringapis.entities.UserProfile;
import com.example.mentoringapis.errors.ClientBadRequestError;
import com.example.mentoringapis.errors.FirebaseError;
import com.example.mentoringapis.errors.MentoringAuthenticationError;
import com.example.mentoringapis.models.upStreamModels.*;
import com.example.mentoringapis.repositories.AccountsRepository;
import com.example.mentoringapis.repositories.DepartmentRepository;
import com.example.mentoringapis.security.CustomUserDetails;
import com.example.mentoringapis.security.JwtTokenProvider;
import com.example.mentoringapis.utilities.ValidatorUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccountsRepository accountsRepository;
    private final DepartmentRepository departmentRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final FirebaseAuth firebaseAuth;
    private final FireStoreService fireStoreService;

    public CustomUserDetails getUserDetailsByUuid(UUID uuid) {
        var accountInfo = accountsRepository.findById(uuid);
        return accountInfo.filter(acc -> !Account.Status.INVALIDATE.name().equals(acc.getStatus())).map(CustomUserDetails::new).orElse(null);
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

    public SignInRes signInWithGoogle(SignInWithGoogleRequest request) throws FirebaseAuthException, FirebaseError, MentoringAuthenticationError, ClientBadRequestError {
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

        //invalidated account cannot login
        if(accountOptional.isPresent() && Account.Status.INVALIDATE.name().equals(accountOptional.get().getStatus()))
            throw new ClientBadRequestError(String.format("Attempt to login with INVALIDATE account [%s]", accountOptional.get().getId()));

        //create account to db if not exist
        return accountOptional
                .map(account -> {
                    if(account.getRole().equals(Account.Role.STAFF.name()) && Account.Status.WAITING.name().equals(account.getStatus())){
                        account.setStatus(Account.Status.ACTIVATED.name());
                        accountsRepository.save(account);
                    }
                    return SignInRes.buildFromAccount(account, jwtTokenProvider);
                })
                .orElseGet(() -> {
                    //create new STUDENT account in db
                    var newAccount = createNewAccountAndProfile(request.getEmail(),
                            request.getFullName(), request.getAvatarUrl(), Account.Role.STUDENT,
                            null, null);
                    return SignInRes.buildFromAccount(newAccount, jwtTokenProvider);
                });
    }

    private Account createNewAccountAndProfile(String email,
                                               String fullName, String avatarUrl, Account.Role role, Department department, String phoneNum) {
        var newAccount = new Account(email,
                role);
        if(Account.Role.MENTOR.equals(role) || Account.Role.STAFF.equals(role)){
            newAccount.setStatus(Account.Status.WAITING.name());
        }else{
            newAccount.setStatus(Account.Status.ACTIVATED.name());
        }

        var newProfile = new UserProfile();
        newProfile.setGender(Gender.others);
        newProfile.setFullName(fullName);
        newAccount.setUserProfile(newProfile);
        newProfile.setAccount(newAccount);
        newProfile.setAvatarUrl(avatarUrl);
        newProfile.setPhoneNum(phoneNum);

        newAccount.setDepartment(department);

        var createdAccount = accountsRepository.save(newAccount);

        fireStoreService.updateUserProfile(fullName, avatarUrl, role.name(), email, createdAccount.getId());

        return createdAccount;
    }

    public MentorAccountResponse createMentorAccount(CreateMentorAccountRequest request) throws MentoringAuthenticationError {
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
        var newMentorAccount = createNewAccountAndProfile(request.getEmail(),
                request.getFullName(), request.getAvatarUrl(), Account.Role.MENTOR,
                null, request.getPhoneNumber());
        return MentorAccountResponse.fromAccountEntity(newMentorAccount);
    }

    public String createStaffAccount(CreateStaffAccountRequest request) throws MentoringAuthenticationError, ClientBadRequestError {
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
        var department = ofNullable(request.getDepartmentId())
                .flatMap(departmentRepository::findById)
                .orElse(null);
        if(department == null)
            throw new ClientBadRequestError(String.format("Cannot find department with id: %s", request.getDepartmentId()));
        var newStaffAccount = createNewAccountAndProfile(request.getEmail(),
                request.getFullName(), request.getAvatarUrl(), Account.Role.STAFF, department, request.getPhoneNumber());
        return newStaffAccount.getEmail();
    }


}
