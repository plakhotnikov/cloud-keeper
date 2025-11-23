package com.plakhotnikov.cloudkeeper.storage.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseReqDto implements ExtendedStorageDto {
    @NotNull(message = "User must be authenticated")
    private long userId;
    @NotNull(message = "Path must not be null")
    private String path;
    @NotNull(message = "Object name must not be null")
    private String objName = "";

    public BaseReqDto(long userId, String path) {
        this.userId = userId;
        this.path = path;
    }
}
