package com.plakhotnikov.cloudkeeper.storage;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("minio")
public class MinioConfig {


    @Bean
    @ConfigurationProperties(prefix = "minio.client")
    public MinioClientProperties minioClientProperties(){
        return new MinioClientProperties();
    }

    @Bean
    public MinioClient minioClient(MinioClientProperties minioClientProperties){
        return MinioClient.builder()
                .endpoint(minioClientProperties.getEndpoint())
                .credentials(minioClientProperties.getUser(), minioClientProperties.getPassword())
                .build();
    }




}
