package com.plakhotnikov.cloudkeeper.storage.config.handler;

import com.plakhotnikov.cloudkeeper.storage.model.dto.MkDirDto;
import jakarta.validation.Valid;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;


public class MkDirRequestArgumentResolver extends BaseRequestArgumentResolver {


    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(MkDirRequest.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        MkDirDto reqDto = new MkDirDto(
                getUserId(),
                getPath(webRequest),
                getObjName(webRequest)
        );

        if (parameter.hasParameterAnnotation(Valid.class)) {
            addBindingResult(reqDto, parameter.getParameterName(), webRequest, binderFactory, mavContainer);
        }

        return reqDto;

    }

}
