package com.example.mentoringapis.models.downStreamModels;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignInResponse extends FirebaseBaseResponse{
    String email;
    String localId;
}
