package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

/**
 * Web配置类
 * 核心用途：配置静态资源访问路径，支持文件上传后的访问
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:}")
    private String uploadPath;

    /**
     * 配置静态资源处理器
     * 核心逻辑：将/files/**路径映射到文件上传目录，支持通过URL访问上传的文件
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String actualPath;
        if (uploadPath != null && !uploadPath.isEmpty()) {
            actualPath = uploadPath;
        } else {
            // 使用项目根目录下的files文件夹
            String projectRoot = System.getProperty("user.dir");
            actualPath = Paths.get(projectRoot, "files").toString();
        }
        
        // 确保路径以/结尾
        if (!actualPath.endsWith(File.separator) && !actualPath.endsWith("/")) {
            actualPath += File.separator;
        }
        
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + actualPath);
    }
}

