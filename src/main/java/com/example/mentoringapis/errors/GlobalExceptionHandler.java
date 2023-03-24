package com.example.mentoringapis.errors;

import com.example.mentoringapis.models.downStreamModels.FirebaseBaseResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Getter
    @Setter
    @Builder
    public static class ErrorDetail{
        private String field;
        private String errorDetail;
    }
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        var errorList = ex.getBindingResult().getAllErrors().stream().map(
                error -> ErrorDetail.builder()
                        .field(((FieldError) error).getField())
                        .errorDetail(error.getDefaultMessage())
                        .build()
        ).collect(Collectors.toList());
        return new ResponseEntity<>(errorList, HttpStatus.BAD_REQUEST);
    }


}
