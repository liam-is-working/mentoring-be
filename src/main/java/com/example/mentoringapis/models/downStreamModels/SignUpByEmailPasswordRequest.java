package com.example.mentoringapis.models.downStreamModels;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class SignUpByEmailPasswordRequest extends FirebaseBaseRequest{
    private String email;
    private String password;
}
