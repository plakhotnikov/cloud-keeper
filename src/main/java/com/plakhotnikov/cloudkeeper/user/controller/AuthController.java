package com.plakhotnikov.cloudkeeper.user.controller;

import com.plakhotnikov.cloudkeeper.user.model.UserDto;
import com.plakhotnikov.cloudkeeper.user.service.UserService;
import com.plakhotnikov.cloudkeeper.user.validation.UserDtoValidator;
import com.plakhotnikov.cloudkeeper.validation.ValidationUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/auth")
@Profile({"web"})
public class AuthController {

    private final UserService userService;

    private final UserDtoValidator userDtoValidator;

    @Autowired
    public AuthController(UserService userService, UserDtoValidator userDtoValidator) {
        this.userService = userService;
        this.userDtoValidator = userDtoValidator;
    }

    @GetMapping("/login")
    public String logIn(@ModelAttribute("userDto") UserDto userDto) {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(@ModelAttribute("userDto") UserDto userDto) {
        return "auth/registration";
    }

    @PostMapping("/register")
    public String performRegistration(@ModelAttribute("userDto") @Valid UserDto userDto,
                                            BindingResult bindingResult,
                                            Model model) {

        userDtoValidator.validate(userDto, bindingResult);

        if (bindingResult.hasErrors()) {
            ValidationUtil.extractErrorsInfo(bindingResult).forEach(log::warn);
            model.addAttribute("errorMessage", ValidationUtil.chooseMainErrorMessage(bindingResult));
            model.addAttribute("userDto", userDto);
            return "auth/registration";
        }

        userService.register(userDto);
        return "redirect:/auth/login";
    }

}
