package com.plakhotnikov.cloudkeeper.user.validation;

import com.plakhotnikov.cloudkeeper.user.model.User;
import com.plakhotnikov.cloudkeeper.user.model.UserDto;
import com.plakhotnikov.cloudkeeper.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
@Profile("auth")
public class UserDtoValidator implements Validator {

    private final UserService service;

    @Autowired
    public UserDtoValidator(UserService service) {
        this.service = service;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserDto userDto = (UserDto) target;

        Optional<User> optUser = service.findByUsername(userDto.getUsername());

        if (optUser.isPresent()) {
            errors.rejectValue("username",
                    "NORMAL", "User with username '%s' already exists".formatted(userDto.getUsername()));
        }

        if (!passwordsMatch(userDto)) {
            errors.rejectValue("password", "NORMAL", "Passwords do not match");
        }

    }

    private static boolean passwordsMatch(UserDto userDto) {
        return userDto.getPassword().matches(userDto.getPasswordConfirmation());
    }
}
