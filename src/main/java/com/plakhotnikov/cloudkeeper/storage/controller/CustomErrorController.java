package com.plakhotnikov.cloudkeeper.storage.controller;

import com.plakhotnikov.cloudkeeper.security.SecurityConfig;
import com.plakhotnikov.cloudkeeper.security.SecurityUserDetails;
import com.plakhotnikov.cloudkeeper.user.model.User;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/error")
@Profile({"web"})
public class CustomErrorController implements ErrorController {

    private final SecurityConfig securityConfig;

    @Autowired
    public CustomErrorController(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    @RequestMapping
    public String handleErrors(HttpServletRequest request, Model model) {
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object statusCodeObject = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = -1;
        if (statusCodeObject instanceof String) {
            statusCode = Integer.parseInt((String) statusCodeObject);
        } else if (statusCodeObject instanceof Integer) {
            statusCode = (Integer) statusCodeObject;
        }

        Object resourcePath = model.getAttribute("resourcePath");
        if (resourcePath instanceof String) {
            requestUri = (String) resourcePath;
        }

        if (request.getUserPrincipal() != null) {
            User user = ((SecurityUserDetails) ((UsernamePasswordAuthenticationToken) request.getUserPrincipal()).getPrincipal()).getUser();
            log.warn("Attempted to access '{}', status code '{}', username '{}'", requestUri, statusCode, user.getUsername());
        } else {
            log.warn("Attempted to access '{}', status code '{}', user is anonymous", requestUri, statusCode);
        }

        if (isUriForAnonymousOnly(requestUri) && statusCode == HttpStatus.FORBIDDEN.value()) {
            return "redirect:/";
        } else if (isLogoutUri(requestUri) && statusCode == HttpStatus.FORBIDDEN.value()) {
            return "redirect:%s".formatted(securityConfig.getWelcomeUrl());
        }

        String errorMessage = "How did you get here? Anyway, go back to home page it's definitely better out there :)";
        model.addAttribute("errorMessage", errorMessage);
        return "error";
    }

    private boolean isUriForAnonymousOnly(String requestUri) {
        return requestUri != null &&
                (requestUri.contains(securityConfig.getRegisterUrl())
                        || requestUri.contains(securityConfig.getLoginUrl())
                        || requestUri.contains(securityConfig.getWelcomeUrl()));
    }

    private boolean isLogoutUri(String requestUri) {
        return requestUri != null && requestUri.contains(securityConfig.getLogoutUrl());
    }

}
