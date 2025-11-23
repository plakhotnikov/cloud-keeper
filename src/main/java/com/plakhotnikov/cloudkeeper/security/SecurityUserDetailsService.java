package com.plakhotnikov.cloudkeeper.security;

import com.plakhotnikov.cloudkeeper.user.model.User;
import com.plakhotnikov.cloudkeeper.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Profile("auth")
public class SecurityUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    @Autowired
    public SecurityUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = repository.findByUsername(username);
        return user
                .map(SecurityUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(username + "not found"));
    }

}
