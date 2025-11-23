package com.plakhotnikov.cloudkeeper.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
@Profile("web")
public class ValidationPatternsController {

    private final ValidationProperties validationProperties;

    @Autowired
    public ValidationPatternsController(Environment env, ValidationProperties validationProperties) {
        this.validationProperties = validationProperties;
    }

    @ModelAttribute("usernamePattern")
    public String usernameValidatePattern() {
        return validationProperties.patterns.username;
    }

    @ModelAttribute("passwordPattern")
    public String passwordValidatePattern() {
        return validationProperties.patterns.password;
    }

    @ModelAttribute("objnamePattern")
    public String objnameValidatePattern() {
        return validationProperties.patterns.objname;
    }

    @ModelAttribute("searchPattern")
    public String searchValidatePattern() {
        return validationProperties.patterns.search;
    }

    @ModelAttribute("usernameMessage")
    public String usernameValidateMessage() {
        return validationProperties.messages.username;
    }

    @ModelAttribute("passwordMessage")
    public String passwordValidateMessage() {
        return validationProperties.messages.password;
    }

    @ModelAttribute("objnameMessage")
    public String objnameValidateMessage() {
        return validationProperties.messages.objname;
    }

    @ModelAttribute("searchMessage")
    public String searchValidateMessage() {
        return validationProperties.messages.search;
    }

}
