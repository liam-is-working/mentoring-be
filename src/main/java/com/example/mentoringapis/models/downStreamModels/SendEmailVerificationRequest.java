package com.example.mentoringapis.models.downStreamModels;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendEmailVerificationRequest {
    String requestType;
    String idToken;
}
