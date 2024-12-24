package com.ruoyi.fms.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.fms.service.FtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/fms/ftp")
public class FtpController {

    private static final Logger log = LoggerFactory.getLogger(FtpController.class);

    // 临时目录常量
    private static final String TEMP_DIR = "C:/temp/";

    @Autowired
    private FtpService ftpService;

    /**
     * 文件上传接口
     *
     * @param file 上传的文件
     * @return 上传结果
     */
    @Anonymous
    @PostMapping("/upload")
    public Response uploadFile(@RequestParam("file") MultipartFile file) {
        String localFilePath = TEMP_DIR + file.getOriginalFilename();
        try {
            // 创建临时目录（如果不存在）
            File tempDir = new File(TEMP_DIR);
            if (!tempDir.exists()) {
                boolean created = tempDir.mkdirs();
                log.info("创建临时目录: {}", created ? "成功" : "失败");
            }

            // 保存文件到本地
            file.transferTo(new File(localFilePath));
            log.info("文件保存到本地临时路径: {}", localFilePath);

            // 上传文件到 FTP
            ftpService.uploadFile(localFilePath, file.getOriginalFilename());
            log.info("文件成功上传到 FTP: {}", file.getOriginalFilename());

            // 清理临时文件
            new File(localFilePath).delete();
            log.info("本地临时文件已清理: {}", localFilePath);

            return Response.success("文件上传成功");
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return Response.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 文件下载接口
     *
     * @param fileName 文件名
     * @return 下载结果
     */
    @Anonymous
    @GetMapping("/download")
    public Response downloadFile(@RequestParam String fileName) {
        String localFilePath = TEMP_DIR + fileName;
        try {
            // 从 FTP 下载文件
            ftpService.downloadFile(fileName, localFilePath);
            log.info("文件成功从 FTP 下载: {}", fileName);

            return Response.success("文件下载成功，保存到: " + localFilePath);
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            return Response.error("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 标准化响应类
     */
    public static class Response {
        private int code;
        private String msg;

        public Response(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public static Response success(String msg) {
            return new Response(200, msg);
        }

        public static Response error(String msg) {
            return new Response(500, msg);
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
