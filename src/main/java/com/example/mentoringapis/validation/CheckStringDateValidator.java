package com.example.mentoringapis.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CheckStringDateValidator implements ConstraintValidator<CheckStringDate, String> {

    private String format;

    @Override
    public void initialize(CheckStringDate constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        format = constraintAnnotation.format();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if(s == null)
            return true;
        try {
            LocalDate.parse(s, DateTimeFormatter.ofPattern(format));
        }catch (DateTimeParseException parseException){
            return false;
        }
        return true;
    }
}
