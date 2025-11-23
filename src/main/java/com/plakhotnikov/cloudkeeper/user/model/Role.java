package com.plakhotnikov.cloudkeeper.user.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    USER,
    PROMETHEUS,
    ADMIN,
    ANONYMOUS;

    @Override
    public String getAuthority() {
        return this.name();
    }
}
