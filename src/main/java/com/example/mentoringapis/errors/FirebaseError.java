package com.example.mentoringapis.errors;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class FirebaseError extends Throwable{
    int code;
    List<String> errorMessages;
}
