package com.plakhotnikov.cloudkeeper.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MinioClientProperties {
    private String endpoint;
    private String user;
    private String password;
}
