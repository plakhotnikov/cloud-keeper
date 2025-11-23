package com.plakhotnikov.cloudkeeper.user.service;


import com.plakhotnikov.cloudkeeper.user.mapper.UserMapper;
import com.plakhotnikov.cloudkeeper.user.model.User;
import com.plakhotnikov.cloudkeeper.user.model.UserDto;
import com.plakhotnikov.cloudkeeper.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Profile("auth")
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public Optional<User> findByUsername(String username){
        return repository.findByUsername(username);
    }

    @Transactional
    public void register(UserDto user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repository.save(userMapper.mapToEntity(user));
    }

}
