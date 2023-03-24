package com.example.mentoringapis.models.downStreamModels;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SendEmailVerificationResponse extends FirebaseBaseResponse{
    private String email;
}
