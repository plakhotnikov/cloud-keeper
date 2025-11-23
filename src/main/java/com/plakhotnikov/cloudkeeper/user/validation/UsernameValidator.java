package com.plakhotnikov.cloudkeeper.user.validation;

import com.plakhotnikov.cloudkeeper.validation.ValidationProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("auth")
public class UsernameValidator implements ConstraintValidator<UsernameConstraint, String> {

    private final ValidationProperties validationProperties;

    @Autowired
    public UsernameValidator(ValidationProperties validationProperties) {
        this.validationProperties = validationProperties;
    }

    @Override
    public void initialize(UsernameConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && value.matches(validationProperties.patterns.username);
    }
}
