package com.plakhotnikov.cloudkeeper.validation;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("auth")
public class ValidationConfig {

    @Bean
    @ConfigurationProperties(prefix = "validation")
    public ValidationProperties validationProperties(){
        return new ValidationProperties();
    }


}
