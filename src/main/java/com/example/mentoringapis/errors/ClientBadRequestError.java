package com.example.mentoringapis.errors;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class ClientBadRequestError extends Throwable {
    final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    String errorMessages;

    public ClientBadRequestError(String errorMessages) {
        this.errorMessages = errorMessages;
    }
}
