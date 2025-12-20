package com.example.demo.controller;

import com.example.demo.commom.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@CrossOrigin
public class FileController {

    @Value("${file.upload.path:}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 允许的图片格式
        String[] allowedTypes = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
        boolean isValidType = false;
        for (String type : allowedTypes) {
            if (suffix.toLowerCase().equals(type)) {
                isValidType = true;
                break;
            }
        }
        
        if (!isValidType) {
            return Result.error("只支持图片格式：jpg, jpeg, png, gif, bmp, webp");
        }

        // 检查文件大小（限制为5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("文件大小不能超过5MB");
        }

        try {
            // 确定上传目录路径
            File uploadDir;
            if (uploadPath != null && !uploadPath.isEmpty()) {
                // 如果配置了路径，使用配置的路径
                uploadDir = new File(uploadPath);
            } else {
                // 否则使用项目根目录下的files文件夹
                String projectRoot = System.getProperty("user.dir");
                uploadDir = Paths.get(projectRoot, "files").toFile();
            }
            
            // 创建上传目录（如果不存在）
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    return Result.error("无法创建上传目录：" + uploadDir.getAbsolutePath());
                }
            }

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + suffix;
            File destFile = new File(uploadDir, fileName);

            // 保存文件
            file.transferTo(destFile);

            // 返回文件访问路径
            String filePath = "/files/" + fileName;
            return Result.success(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}

