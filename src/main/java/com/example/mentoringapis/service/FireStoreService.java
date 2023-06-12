package com.example.mentoringapis.service;

import com.example.mentoringapis.entities.Account;
import com.example.mentoringapis.entities.UserProfile;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class FireStoreService {
    private final Firestore firestore;
    private static final String USER_PATH = "Users";

    public void updateUserProfile(String fullName, String avatarUrl, String role, String email, UUID id){
        firestore
                .collection(USER_PATH)
                .document(id.toString())
                .set(generateDocumentDataForAccount(fullName, avatarUrl, role, email));
    }

    private Map<String, Object> generateDocumentDataForAccount(String fullName, String avatarUrl, String role, String email){
        return Map.of(
                "avatarUrl", ofNullable(avatarUrl).orElse(""),
                "email", ofNullable(email).orElse(""),
                "role", ofNullable(role).orElse(""),
                "fullName", ofNullable(fullName).orElse("")
        );
    }
}
