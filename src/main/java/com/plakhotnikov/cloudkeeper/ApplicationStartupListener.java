package com.plakhotnikov.cloudkeeper;

import com.plakhotnikov.cloudkeeper.storage.repository.MinioRepository;
import com.plakhotnikov.cloudkeeper.user.model.Role;
import com.plakhotnikov.cloudkeeper.user.model.UserDto;
import com.plakhotnikov.cloudkeeper.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"dev", "prod"})
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {


    private final String prometheusUserPassword;
    private final MinioRepository minioRepository;
    private final UserService userService;

    @Autowired
    public ApplicationStartupListener(MinioRepository minioRepository,
                                      UserService userService,
                                      @Value("${prometheus.user-password}") String prometheusUserPassword) {
        this.minioRepository = minioRepository;
        this.userService = userService;
        this.prometheusUserPassword = prometheusUserPassword;
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        minioRepository.createDefaultBucket();
        createPrometheusUser();
    }

    private void createPrometheusUser() {

        if (userService.findByUsername("prometheus").isEmpty()){
            UserDto userDto = new UserDto(
                    "prometheus",
                    prometheusUserPassword,
                    prometheusUserPassword,
                    Role.PROMETHEUS
            );
            userService.register(userDto);
            log.info("Prometheus user registered successfully");
        } else {
            log.info("Prometheus user already exists");
        }

    }


}
