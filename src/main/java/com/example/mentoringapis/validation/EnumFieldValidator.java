package com.example.mentoringapis.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class EnumFieldValidator implements ConstraintValidator<EnumField, String> {

    private String[] availableValues;

    @Override
    public void initialize(EnumField constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        availableValues = constraintAnnotation.availableValues();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if(s == null)
            return false;
        return Arrays.asList(availableValues).contains(s);
    }
}
