package com.plakhotnikov.cloudkeeper.storage.config.handler;

import com.plakhotnikov.cloudkeeper.storage.model.dto.UploadDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UploadRequestArgumentResolver extends BaseRequestArgumentResolver {

    private static final String MULTIPART_PARAM_NAME = "files";


    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(UploadRequest.class) != null;
    }


    @Override
    public Object resolveArgument(MethodParameter parameter,
                                     ModelAndViewContainer mavContainer,
                                     NativeWebRequest webRequest,
                                     WebDataBinderFactory binderFactory) throws Exception {

        UploadDto reqDto = new UploadDto(
                getUserId(),
                getPath(webRequest),
                getFiles(webRequest)
        );

        if (parameter.hasParameterAnnotation(Valid.class)) {
            addBindingResult(reqDto, parameter.getParameterName(), webRequest, binderFactory, mavContainer);
        }

        return reqDto;

    }

    private List<MultipartFile> getFiles(NativeWebRequest webRequest) {
        MultipartHttpServletRequest multipartRequest = webRequest.getNativeRequest(MultipartHttpServletRequest.class);
        if (multipartRequest == null){
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

            if (request != null){
                String uri = request.getRequestURI();
                long userId = getUserId();
                log.warn("Failed to extract multipart data from request, uri: {}, user id: {}", uri, userId);
            } else {
                log.warn("Failed to extract multipart data from request");
            }
            return new ArrayList<>();
        }
        return multipartRequest.getFiles(MULTIPART_PARAM_NAME);
    }


}
