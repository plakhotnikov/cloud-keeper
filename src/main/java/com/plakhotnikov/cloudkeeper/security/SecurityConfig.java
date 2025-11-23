package com.plakhotnikov.cloudkeeper.security;


import com.plakhotnikov.cloudkeeper.user.model.Role;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@Profile("auth")
@Getter
public class SecurityConfig {

    private final String registerUrl = "/auth/register";
    private final String loginUrl = "/auth/login";
    private final String logoutUrl = "/auth/logout";
    private final String welcomeUrl = "/welcome";
    private final String defaultUrl = "/";
    private final String[] unsecuredUrls = new String[]{
            welcomeUrl,
            "/error",
            "/css/**",
            "/js/**",
            "/common/**",
            "/cloud-data.ico"
    };

    @Bean
    @Order(1)
    public SecurityFilterChain basicAuthChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/prometheus")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().hasRole(Role.PROMETHEUS.name())
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(registerUrl, loginUrl, welcomeUrl).anonymous()
                        .requestMatchers(unsecuredUrls).permitAll()
                        .requestMatchers("/actuator/**").hasRole(Role.ADMIN.name())
                        .requestMatchers("/**").authenticated())
                .formLogin(formLogin -> {
                    formLogin.loginPage(welcomeUrl);
                    formLogin.loginProcessingUrl(loginUrl);
                    formLogin.defaultSuccessUrl(defaultUrl, true);
                    formLogin.failureUrl(loginUrl + "?error");
                })
                .logout(logout -> {
                    logout.logoutUrl(logoutUrl);
                    logout.logoutSuccessUrl(welcomeUrl);
                })
                .build();
    }

    @Bean
    @Autowired
    public AuthenticationManager authenticationManager(SecurityUserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    static GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }
}
