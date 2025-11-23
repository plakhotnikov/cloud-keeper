package com.plakhotnikov.cloudkeeper.storage.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MkDirDto implements ExtendedStorageDto {

    @NotNull(message = "User must be authenticated")
    private long userId;
    @NotNull(message = "Path must not be null")
    private String path;
    @NotBlank(message = "Object name must noy be blank")
    private String objName;

}
