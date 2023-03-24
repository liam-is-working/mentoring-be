package com.example.mentoringapis.repositories;

import com.example.mentoringapis.models.downStreamModels.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Repository
public class FirebaseAuthRepository {
    private static final String appKey = "AIzaSyDt0U5OB0F0vlX35CLhi_o6vnOXFtdZZVQ";
    private final WebClient webClient = WebClient.builder().baseUrl("https://identitytoolkit.googleapis.com/v1/").build();

    public Mono<SignUpByEmailPasswordResponse> signUpByEmailPassword(String email, String password){
        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.pathSegment("accounts:signUp").queryParam("key", appKey).build())
                .bodyValue(SignUpByEmailPasswordRequest.builder().password(password).email(email).build())
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(SignUpByEmailPasswordResponse.class));
    }

    public Mono<SendEmailVerificationResponse> sendEmailVerification(String idToken){
        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.pathSegment("accounts:sendOobCode").queryParam("key", appKey).build())
                .bodyValue(SendEmailVerificationRequest.builder().requestType("VERIFY_EMAIL").idToken(idToken).build())
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(SendEmailVerificationResponse.class));
    }

//    public Mono<SignUpByEmailPasswordResponse> signUpAnonymously(){
//        return webClient
//                .post()
//                .uri("accounts:signUp?key=" + appKey)
//                .bodyValue(SignUpByEmailPasswordRequest.builder().build())
//                .retrieve()
//                .bodyToMono(SignUpByEmailPasswordResponse.class)
//                .onErrorComplete();
//    }

    public Mono<SignInResponse> signInWithEmailAndPassword(String email, String password){
        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.pathSegment("accounts:signInWithPassword").queryParam("key", appKey).build())
                .bodyValue(SignUpByEmailPasswordRequest.builder().password(password).email(email).build())
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(SignInResponse.class));
    }

    public Mono<EmailVerificationResponse> verifyEmail(String oobCode){
        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.pathSegment("accounts:update").queryParam("key", appKey).build())
                .bodyValue(Map.of("oobCode", oobCode))
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(EmailVerificationResponse.class));
    }


}
