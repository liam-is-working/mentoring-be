package com.example.mentoringapis.errors;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ResourceNotFoundException extends Throwable{
    HttpStatus httpStatus;
    String errorMessages;

    public ResourceNotFoundException(String errorMessages){
        httpStatus = HttpStatus.NOT_FOUND;
        this.errorMessages = errorMessages;
    }
}
