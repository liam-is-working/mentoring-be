package com.example.mentoringapis.models.downStreamModels;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class FirebaseErrorResponse implements Serializable {
    private String code;
    private String message;
    private List<ErrorDetail> errors;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class ErrorDetail{
        private String message;
        private String domain;
        private String reason;
    }
}
