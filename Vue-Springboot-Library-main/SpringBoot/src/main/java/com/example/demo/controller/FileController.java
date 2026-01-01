package com.example.demo.controller;

import com.example.demo.commom.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传控制器
 * 核心用途：提供文件上传接口，支持图片格式验证和大小限制
 */
@RestController
@RequestMapping("/file")
@CrossOrigin
public class FileController {

    @Value("${file.upload.path:}")
    private String uploadPath;

    /**
     * 上传文件（含文件类型和大小验证）
     * 核心逻辑：验证文件类型（仅支持图片格式）、文件大小（限制5MB）、生成唯一文件名并保存
     * @param file 上传的文件对象
     * @return Result<String> 文件访问路径，失败返回错误信息
     */
    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
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

        // 验证文件大小（限制5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("文件大小不能超过5MB");
        }

        try {
            // 确定上传目录路径
            File uploadDir;
            if (uploadPath != null && !uploadPath.isEmpty()) {
                uploadDir = new File(uploadPath);
            } else {
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
            // 将上传文件保存到目标位置
            File destFile = new File(uploadDir, fileName);
            file.transferTo(destFile);

            String filePath = "/files/" + fileName;
            return Result.success(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}

