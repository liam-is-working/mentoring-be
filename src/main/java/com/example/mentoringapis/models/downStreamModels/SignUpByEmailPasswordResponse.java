package com.example.mentoringapis.models.downStreamModels;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class SignUpByEmailPasswordResponse extends FirebaseBaseResponse{
    private String idToken;
    private String email;
    private String refreshToken;
    private String expiresIn;
    private String localId;
}
