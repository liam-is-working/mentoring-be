package com.example.mentoringapis.configurations;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.StorageClient;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@SpringBootConfiguration
public class MentoringApisConfig {
    @Bean
    FirebaseApp firebaseApp() throws IOException {
        var serviceAccount =
                new ClassPathResource("secret/study-with-mentors-firebase-adminsdk-uss11-7a0f31fcca.json").getInputStream();

         FirebaseOptions options = FirebaseOptions.builder()
                 .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                 .setStorageBucket("study-with-mentors.appspot.com")
                 .build();

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    FirebaseAuth firebaseAuth() throws IOException {
        return FirebaseAuth.getInstance(firebaseApp());
    }

    @Bean
    WebClient webClient(){
        return WebClient.builder().build();
    }

    @Bean
    Bucket firebaseStorage(){
        return StorageClient.getInstance().bucket();
    }

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public PebbleEngine pebbleEngine(){
        return new PebbleEngine.Builder()
                .loader(new ClasspathLoader())
                .build();
    }
}
