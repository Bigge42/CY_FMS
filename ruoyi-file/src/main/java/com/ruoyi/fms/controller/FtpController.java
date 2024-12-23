package com.ruoyi.fms.controller;

import com.ruoyi.fms.service.FtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/fms/ftp")
public class FtpController {

    @Autowired
    private FtpService ftpService;

    // 文件上传接口
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String localFilePath = "C:/temp/" + file.getOriginalFilename();  // 保存到临时路径
            file.transferTo(new File(localFilePath));

            // 上传文件到 FTP
            ftpService.uploadFile(localFilePath, file.getOriginalFilename());
            return "文件上传成功";
        } catch (IOException e) {
            return "文件上传失败: " + e.getMessage();
        }
    }

    // 文件下载接口
    @GetMapping("/download")
    public String downloadFile(@RequestParam String fileName) {
        try {
            String localFilePath = "C:/temp/" + fileName;
            ftpService.downloadFile(fileName, localFilePath);
            return "文件下载成功，保存到: " + localFilePath;
        } catch (IOException e) {
            return "文件下载失败: " + e.getMessage();
        }
    }
}
