package com.community.employeemanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC Configuration.
 * Maps /uploads/** URL path to the physical uploads directory on disk,
 * so employee photos can be served as static resources.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String resourceLocation = "file:" + uploadPath + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);

        // Serve the UI pages that live under `src/main/resources/templates/` as static files.
        // (This project does not use a view engine like Thymeleaf, so these are plain HTML pages.)
        registry.addResourceHandler("/*.html")
                .addResourceLocations("classpath:/templates/");
    }
}