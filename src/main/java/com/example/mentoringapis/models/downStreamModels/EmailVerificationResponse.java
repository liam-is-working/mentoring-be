package com.example.mentoringapis.models.downStreamModels;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class EmailVerificationResponse extends FirebaseBaseResponse{
    String email;
    String localId;
    String displayName;
    String photoUrl;
    boolean emailVerified;
}
