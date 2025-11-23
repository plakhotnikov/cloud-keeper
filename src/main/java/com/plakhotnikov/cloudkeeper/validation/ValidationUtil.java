package com.plakhotnikov.cloudkeeper.validation;

import lombok.experimental.UtilityClass;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class ValidationUtil {

    public static List<String> extractErrorsInfo(BindingResult bindingResult) {
        List<ObjectError> errors = bindingResult.getAllErrors();
        List<String> messages = new ArrayList<>();
        for (ObjectError e : errors) {
            String message;
            if (e instanceof FieldError) {
                message = "Object <%s>, field <%s>: %s".formatted(e.getObjectName(), ((FieldError) e).getField(), e.getDefaultMessage());
            } else {
                message = "Object <%s>: %s".formatted(e.getObjectName(), e.getDefaultMessage());
            }
            messages.add(message);
        }

        return messages;
    }

    public static List<String> extractErrorCodes(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getCode)
                .toList();
    }


    public static String chooseMainErrorMessage(BindingResult bindingResult) {
        List<String> codes = extractErrorCodes(bindingResult);

        if (bindingResult.getAllErrors().isEmpty()){
            throw new RuntimeException("Could not choose main error message from empty binding result");
        }

        boolean isAbuseFrontPresent = codes.stream().anyMatch(x -> !x.equals("NORMAL"));
        if (isAbuseFrontPresent) {
            return "Attempted to abuse front is detected. You think you're such a smart guy, huh?";
        }

        Optional<ObjectError> usernameError = bindingResult.getAllErrors().stream()
                .filter(x-> x.getObjectName().equals("username"))
                .findFirst();
        if (usernameError.isPresent()) {
            return usernameError.get().getDefaultMessage();
        } else {
            return bindingResult.getAllErrors().get(0).getDefaultMessage();
        }

    }

}
