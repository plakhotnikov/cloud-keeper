package com.plakhotnikov.cloudkeeper.storage.controller;

import com.plakhotnikov.cloudkeeper.storage.config.handler.BaseRequest;
import com.plakhotnikov.cloudkeeper.storage.config.handler.MkDirRequest;
import com.plakhotnikov.cloudkeeper.storage.config.handler.RenameRequest;
import com.plakhotnikov.cloudkeeper.storage.config.handler.UploadRequest;
import com.plakhotnikov.cloudkeeper.storage.exception.controller.DtoValidationException;
import com.plakhotnikov.cloudkeeper.storage.model.dto.BaseReqDto;
import com.plakhotnikov.cloudkeeper.storage.model.dto.MkDirDto;
import com.plakhotnikov.cloudkeeper.storage.model.dto.RenameDto;
import com.plakhotnikov.cloudkeeper.storage.model.dto.UploadDto;
import com.plakhotnikov.cloudkeeper.storage.service.MinioService;
import com.plakhotnikov.cloudkeeper.storage.util.PathUtils;
import com.plakhotnikov.cloudkeeper.validation.ValidationUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Slf4j
@Controller
@RequestMapping("/files")
@Profile({"dev", "prod"})
public class ObjectController {

    private final MinioService minioService;

    @Autowired
    public ObjectController(MinioService minioService) {
        this.minioService = minioService;
    }


    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String uploadFiles(@UploadRequest @Valid UploadDto uploadDto,
                              BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ValidationUtil.extractErrorsInfo(bindingResult).forEach(log::warn);
            throw new DtoValidationException("Invalid upload request");
        }

        minioService.upload(uploadDto);

        String path = PathUtils.getEncodedPath(uploadDto);
        String pathParam = path.isBlank() ? "" : "?path=%s".formatted(path);
        return "redirect:/%s".formatted(pathParam);
    }

    @GetMapping(produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadFiles(@BaseRequest @Valid BaseReqDto downloadDto,
                                                  BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ValidationUtil.extractErrorsInfo(bindingResult).forEach(log::warn);
            throw new DtoValidationException("Invalid download request");
        }

        InputStreamResource resource = minioService.download(downloadDto);

        String filename = minioService.isDir(downloadDto)
                ? String.format("%s.zip", downloadDto.getObjName())
                : downloadDto.getObjName();

        String filenameEncoded = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''\"%s\"".formatted(filenameEncoded))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PatchMapping
    public String renameFile(@RenameRequest @Valid RenameDto renameDto,
                             BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ValidationUtil.extractErrorsInfo(bindingResult).forEach(log::warn);
            throw new DtoValidationException("Invalid rename request");
        }

        minioService.rename(renameDto);

        return "redirect:/?path=%s".formatted(PathUtils.getEncodedPath(renameDto));
    }

    @DeleteMapping
    public String deleteFile(@BaseRequest @Valid BaseReqDto deleteDto,
                             BindingResult bindingResult) {


        if (bindingResult.hasErrors()) {
            ValidationUtil.extractErrorsInfo(bindingResult).forEach(log::warn);
            throw new DtoValidationException("Invalid delete request");
        }

        minioService.delete(deleteDto);

        return "redirect:/?path=%s".formatted(PathUtils.getEncodedPath(deleteDto));
    }


    @PostMapping("/create")
    public String createFolder(@MkDirRequest @Valid MkDirDto mkDirDto,
                               BindingResult bindingResult) {


        if (bindingResult.hasErrors()) {
            ValidationUtil.extractErrorsInfo(bindingResult).forEach(log::warn);
            throw new DtoValidationException("Invalid create folder request");
        }

        minioService.createFolder(mkDirDto);

        return "redirect:/?path=%s".formatted(PathUtils.getEncodedPath(mkDirDto));

    }

}
