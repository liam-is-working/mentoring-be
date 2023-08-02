package com.example.mentoringapis.utilities;

import com.example.mentoringapis.errors.MentoringAuthenticationError;
import com.example.mentoringapis.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public class AuthorizationUtils {
    public static UUID getCurrentUserUuid(Authentication authentication) throws MentoringAuthenticationError {
        if(authentication == null)
            throw MentoringAuthenticationError
            .builder()
            .httpStatus(HttpStatus.UNAUTHORIZED)
            .errorMessages("Unauthorized access")
            .build();
        return  ((CustomUserDetails) authentication.getPrincipal()).getAccount().getId();
    }

    public static CustomUserDetails getCurrentUser(Authentication authentication) throws MentoringAuthenticationError {
        if(authentication == null)
            throw MentoringAuthenticationError
                    .builder()
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .errorMessages("Unauthorized access")
                    .build();
        return  ((CustomUserDetails) authentication.getPrincipal());
    }


}
