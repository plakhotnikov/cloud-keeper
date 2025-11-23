package com.plakhotnikov.cloudkeeper.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Constraint(validatedBy = UsernameValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UsernameConstraint {
    String message() default "Invalid username format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
