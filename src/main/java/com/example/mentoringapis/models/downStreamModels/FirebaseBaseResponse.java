package com.example.mentoringapis.models.downStreamModels;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class FirebaseBaseResponse implements Serializable {
    private FirebaseErrorResponse error;
}
