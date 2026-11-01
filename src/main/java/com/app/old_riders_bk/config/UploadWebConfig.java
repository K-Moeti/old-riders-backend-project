package com.app.old_riders_bk.config; // any config package in your project

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves uploaded driver documents at /files/driver-documents/**.
 */
@Configuration
public class UploadWebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads/driver-documents}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/files/driver-documents/**")
                .addResourceLocations(dir.toUri().toString());
    }
}