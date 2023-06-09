package com.example.mentoringapis.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EnumField2Validator implements ConstraintValidator<EnumField2, Object> {

    private final Map<String, List<Object>> typeAnswerPossibleValues =
            Map.of(
                    "YES/NO", List.of("Yes", "No"),
                    "RATING", List.of(1,2,3,4,5)
            );

    private String type;

    @Override
    public void initialize(EnumField2 constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        type = constraintAnnotation.type();
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        return typeAnswerPossibleValues.get(type).contains(o);
    }


}
