package com.example.mentoringapis.errors;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class MentoringAuthenticationError extends Throwable{
    HttpStatus httpStatus;
    String errorMessages;
}
