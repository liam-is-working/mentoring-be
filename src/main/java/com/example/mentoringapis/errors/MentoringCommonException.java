package com.example.mentoringapis.errors;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class MentoringCommonException extends Throwable{
    HttpStatus httpStatus;
    String errorMessages;
}
