package com.example.mentoringapis.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE })
@Retention(RUNTIME)
@Constraint(validatedBy = CheckStringDateValidator.class)
@Documented
public @interface CheckStringDate {
    String message() default "String can't be parsed to DateTime object, please check format";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    String format() default "yyyy-MM-dd";


}
