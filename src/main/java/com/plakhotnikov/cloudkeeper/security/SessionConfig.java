package com.plakhotnikov.cloudkeeper.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plakhotnikov.cloudkeeper.user.model.Role;
import com.plakhotnikov.cloudkeeper.user.model.User;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;

@Configuration
@Profile("auth")
public class SessionConfig implements BeanClassLoaderAware {

    private ClassLoader loader;

    /**
     * Note that the bean name for this bean is intentionally
     * {@code springSessionDefaultRedisSerializer}. It must be named this way to override
     * the default {@link RedisSerializer} used by Spring Session.
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer(objectMapper());
    }

    /**
     * Customized {@link ObjectMapper} to add mix-in for class that doesn't have default
     * constructors
     * @return the {@link ObjectMapper} to use
     */
    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(SecurityJackson2Modules.getModules(this.loader));
        mapper.addMixIn(SecurityUserDetails.class, SecurityUserDetails.class);
        mapper.addMixIn(User.class, User.class);
        mapper.addMixIn(Role.class, Role.class);
        // crutches for serializing flash attributes which are stored in session which is stored in Redis
        mapper.addMixIn(java.util.concurrent.CopyOnWriteArrayList.class, java.util.concurrent.CopyOnWriteArrayList.class);
        mapper.addMixIn(org.springframework.web.servlet.FlashMap.class, org.springframework.web.servlet.FlashMap.class);
        return mapper;
    }

    /*
     * @see
     * org.springframework.beans.factory.BeanClassLoaderAware#setBeanClassLoader(java.lang
     * .ClassLoader)
     */
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.loader = classLoader;
    }

}
