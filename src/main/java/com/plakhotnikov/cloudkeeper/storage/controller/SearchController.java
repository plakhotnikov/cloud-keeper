package com.plakhotnikov.cloudkeeper.storage.controller;

import com.plakhotnikov.cloudkeeper.storage.mapper.BreadcrumbMapper;
import com.plakhotnikov.cloudkeeper.storage.model.dto.BaseReqDto;
import com.plakhotnikov.cloudkeeper.storage.model.dto.BaseRespDto;
import com.plakhotnikov.cloudkeeper.storage.model.Breadcrumb;
import com.plakhotnikov.cloudkeeper.storage.service.MinioService;
import com.plakhotnikov.cloudkeeper.storage.util.PathUtils;
import com.plakhotnikov.cloudkeeper.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Profile({"dev", "prod"})
public class SearchController {

    private final MinioService minioService;
    private final BreadcrumbMapper breadcrumbMapper;

    @Autowired
    public SearchController(MinioService minioService, BreadcrumbMapper breadcrumbMapper) {
        this.minioService = minioService;
        this.breadcrumbMapper = breadcrumbMapper;
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "query", required = false, defaultValue = "") String query,
                         @AuthenticationPrincipal(expression = "getUser") User user,
                         Model model) {

        query = PathUtils.removeSpaces(query);
        BaseReqDto searchReq = new BaseReqDto(user.getId(), "", query);
        List<BaseRespDto> foundObjects = minioService.search(searchReq);
        List<Breadcrumb> breadcrumbs = foundObjects.stream()
                .map(obj -> breadcrumbMapper.mapToModel(obj.getObjName()))
                .toList();

        model.addAttribute("query", query);
        model.addAttribute("user", user);
        model.addAttribute("breadcrumbs", breadcrumbs);

        return "storage/search";

    }


}
