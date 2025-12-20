package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确定上传目录路径（与FileController保持一致）
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
        
        // 配置静态资源访问路径
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + actualPath);
    }
}

