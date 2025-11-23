package com.plakhotnikov.cloudkeeper.storage.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenameDto implements ExtendedStorageDto {

    @NotNull(message = "User must be authenticated")
    private long userId;
    @NotNull(message = "Path must not be null")
    private String path;
    @NotBlank(message = "Object to rename must not be blank")
    private String objName;
    @NotBlank(message = "New object name must not be blank")
    private String newObjName;

}
