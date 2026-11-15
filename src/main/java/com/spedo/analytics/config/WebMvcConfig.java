package com.spedo.analytics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Priority 1: Serve directly from root ./frontend/ for real-time live editing in VS Code
        File frontendDir = new File("frontend");
        if (frontendDir.exists() && frontendDir.isDirectory()) {
            registry.addResourceHandler("/**")
                    .addResourceLocations("file:./frontend/", "classpath:/static/")
                    .setCachePeriod(0);
        } else {
            registry.addResourceHandler("/**")
                    .addResourceLocations("classpath:/static/");
        }
    }
}
