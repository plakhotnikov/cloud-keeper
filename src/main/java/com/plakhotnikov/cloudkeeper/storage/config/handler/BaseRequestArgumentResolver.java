package com.plakhotnikov.cloudkeeper.storage.config.handler;

import com.plakhotnikov.cloudkeeper.security.SecurityUserDetails;
import com.plakhotnikov.cloudkeeper.storage.model.dto.BaseReqDto;
import com.plakhotnikov.cloudkeeper.storage.util.PathUtils;
import com.plakhotnikov.cloudkeeper.user.model.User;
import jakarta.validation.Valid;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


public class BaseRequestArgumentResolver implements HandlerMethodArgumentResolver {


    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(BaseRequest.class) != null;
    }


    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        BaseReqDto reqDto = new BaseReqDto(
                getUserId(),
                getPath(webRequest),
                getObjName(webRequest)
        );

        if (parameter.hasParameterAnnotation(Valid.class)) {
            addBindingResult(reqDto, parameter.getParameterName(), webRequest, binderFactory, mavContainer);
        }

        return reqDto;
    }


    protected static String getPath(NativeWebRequest webRequest) {
        String path = webRequest.getParameter("path");
        if (path == null){
            return "";
        }
        return PathUtils.normalize(path);
    }

    protected static String getObjName(NativeWebRequest webRequest) {
        String objName = webRequest.getParameter("objName");
        if (objName == null){
            return "";
        }
        return PathUtils.normalize(objName);
    }

    protected static long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user;
        if (authentication != null) {
            user = ((SecurityUserDetails) authentication.getPrincipal()).getUser();
        } else {
            throw new RuntimeException("Failed to recognize user from request");
        }

        return user.getId();
    }

    protected static void addBindingResult(Object target, String objectName, NativeWebRequest webRequest, WebDataBinderFactory binderFactory, ModelAndViewContainer mavContainer) throws Exception {
        WebDataBinder binder = binderFactory.createBinder(webRequest, target, objectName);

        binder.validate();
        BindingResult bindingResult = binder.getBindingResult();
        //add the binding result to the mavContainer.
        //the key has to be prefixed with BindingResult.MODEL_KEY_PREFIX
        mavContainer.addAttribute(String.format("%s%s", BindingResult.MODEL_KEY_PREFIX, bindingResult.getObjectName()), bindingResult);
    }

}
