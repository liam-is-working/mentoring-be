package com.example.mentoringapis.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE })
@Retention(RUNTIME)
@Constraint(validatedBy = EnumFieldValidator.class)
@Documented
public @interface EnumField {
    String message() default "String should be in list";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    String[] availableValues();


}
