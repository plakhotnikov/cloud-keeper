package com.plakhotnikov.cloudkeeper.storage.config;

import com.plakhotnikov.cloudkeeper.storage.config.handler.BaseRequestArgumentResolver;
import com.plakhotnikov.cloudkeeper.storage.config.handler.MkDirRequestArgumentResolver;
import com.plakhotnikov.cloudkeeper.storage.config.handler.RenameRequestArgumentResolver;
import com.plakhotnikov.cloudkeeper.storage.config.handler.UploadRequestArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@Profile("web")
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(
            List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new BaseRequestArgumentResolver());
        argumentResolvers.add(new RenameRequestArgumentResolver());
        argumentResolvers.add(new MkDirRequestArgumentResolver());
        argumentResolvers.add(new UploadRequestArgumentResolver());
    }
}
