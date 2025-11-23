package com.plakhotnikov.cloudkeeper.storage.controller;

import com.plakhotnikov.cloudkeeper.storage.config.handler.BaseRequest;
import com.plakhotnikov.cloudkeeper.storage.exception.repository.MinioRepositoryException;
import com.plakhotnikov.cloudkeeper.storage.exception.service.NoSuchFolderException;
import com.plakhotnikov.cloudkeeper.storage.mapper.BreadcrumbMapper;
import com.plakhotnikov.cloudkeeper.storage.model.Breadcrumb;
import com.plakhotnikov.cloudkeeper.storage.model.StorageInfo;
import com.plakhotnikov.cloudkeeper.storage.model.dto.BaseReqDto;
import com.plakhotnikov.cloudkeeper.storage.model.dto.BaseRespDto;
import com.plakhotnikov.cloudkeeper.storage.service.MinioService;
import com.plakhotnikov.cloudkeeper.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
@Profile({"dev", "prod"})
public class HomeController {

    private final MinioService minioService;
    private final BreadcrumbMapper breadcrumbMapper;

    @Autowired
    public HomeController(MinioService minioService, BreadcrumbMapper breadcrumbMapper) {
        this.minioService = minioService;
        this.breadcrumbMapper = breadcrumbMapper;
    }

    @GetMapping("/welcome")
    public String getWelcomePage() {
        return "welcome";
    }


    @GetMapping("/")
    public String getHomePage(@BaseRequest BaseReqDto reqDto,
                       @AuthenticationPrincipal(expression = "getUser") User user,
                       Model model) {

        String path = reqDto.getPath();

        boolean isDir;
        try {
            isDir = minioService.isDir(reqDto);
            if (!isDir && !path.isEmpty()) {
                log.info("Requested path '{}' does not exist", path);
                throw new NoSuchFolderException(path);
            }
        } catch (MinioRepositoryException e) {
            log.info("Requested path '{}' is not valid. Caught exception: {}", path, e.getMessage());
            throw new NoSuchFolderException(path);
        }

        Breadcrumb breadcrumb = breadcrumbMapper.mapToModel(path);
        List<BaseRespDto> userObjects = minioService.list(reqDto);
        StorageInfo storageInfo = minioService.getStorageInfo(new BaseReqDto(reqDto.getUserId(), ""));

        model.addAttribute("path", path);
        model.addAttribute("breadcrumb", breadcrumb);
        model.addAttribute("userObjects", userObjects);
        model.addAttribute("storageInfo", storageInfo);
        model.addAttribute("user", user);


        return "storage/home";
    }

}
