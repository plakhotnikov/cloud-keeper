package com.plakhotnikov.cloudkeeper.storage.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadDto implements StorageDto {

    @NotNull(message = "User must be authenticated")
    private long userId;
    @NotNull(message = "Path must not be null")
    private String path;
    @NotEmpty(message = "List of uploading files must not be empty")
    private List<MultipartFile> files;

}
