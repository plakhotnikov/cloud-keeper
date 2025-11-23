package com.plakhotnikov.cloudkeeper.user.model;


import com.plakhotnikov.cloudkeeper.user.validation.PasswordConstraint;
import com.plakhotnikov.cloudkeeper.user.validation.UsernameConstraint;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDto {

    @UsernameConstraint
    private String username;

    @PasswordConstraint
    private String password;

    @PasswordConstraint
    private String passwordConfirmation;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

}
