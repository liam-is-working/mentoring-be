package com.example.mentoringapis.configurations;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootConfiguration
public class MentoringApisConfig {
    @Bean
    FirebaseApp firebaseApp() throws IOException {
        var serviceAccount =
                new ClassPathResource("secret/study-with-mentors-firebase-adminsdk-uss11-7a0f31fcca.json").getInputStream();

         FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    FirebaseAuth firebaseAuth() throws IOException {
        return FirebaseAuth.getInstance(firebaseApp());
    }
}
