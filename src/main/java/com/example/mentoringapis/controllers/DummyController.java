package com.example.mentoringapis.controllers;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.errors.FirebaseError;
import com.example.mentoringapis.repositories.AccountsRepository;
import com.example.mentoringapis.service.AuthService;
import com.example.mentoringapis.service.MailService;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller("api/dummy")
@RequiredArgsConstructor
public class DummyController {

    private final AuthService authService;
    private final MailService mailService;
    private final AccountsRepository accountsRepository;

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

    @GetMapping()
    public void verifyEmailEndpoint(String mode, String oobCode){

    }
}
