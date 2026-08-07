package com.pages.config;

import com.pages.impl.AuditAwareImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditAware")
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public AuditorAware<String> auditAware(){
        return  new AuditAwareImpl();
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Fixed "user.home" property name (must be lowercase)
        // 2. Added explicit trailing slash for Spring to recognize it as a directory
        String path = "file:" + System.getProperty("user.home") + "/App-media0dir/";

        registry.addResourceHandler("/Media/**")
                .addResourceLocations(path);
    }
}
