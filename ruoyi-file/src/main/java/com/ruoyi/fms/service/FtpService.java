package com.ruoyi.fms.service;

import com.ruoyi.fms.config.FtpConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class FtpService {

    private static final Logger log = LoggerFactory.getLogger(FtpService.class);

    @Autowired
    private FtpConfig ftpConfig;

    public boolean uploadFile(String localFilePath, String remoteFileName) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            connectAndLogin(ftpClient);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.changeWorkingDirectory(ftpConfig.getRemoteDir());

            File localFile = new File(localFilePath);
            try (FileInputStream fis = new FileInputStream(localFile)) {
                boolean result = ftpClient.storeFile(remoteFileName, fis);
                log.info("文件上传状态: {}", result ? "成功" : "失败");
                return result;
            }
        } finally {
            disconnect(ftpClient);
        }
    }

    public boolean downloadFile(String remoteFileName, String localFilePath) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            connectAndLogin(ftpClient);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.changeWorkingDirectory(ftpConfig.getRemoteDir());

            try (FileOutputStream fos = new FileOutputStream(new File(localFilePath))) {
                boolean result = ftpClient.retrieveFile(remoteFileName, fos);
                log.info("文件下载状态: {}", result ? "成功" : "失败");
                return result;
            }
        } finally {
            disconnect(ftpClient);
        }
    }

    public boolean deleteFile(String remoteFileName) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            connectAndLogin(ftpClient);
            boolean result = ftpClient.deleteFile(remoteFileName);
            log.info("文件删除状态: {}", result ? "成功" : "失败");
            return result;
        } finally {
            disconnect(ftpClient);
        }
    }

    private void connectAndLogin(FTPClient ftpClient) throws IOException {
        ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
        boolean login = ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
        if (!login) {
            throw new IOException("FTP 登录失败，请检查用户名和密码");
        }
        log.info("成功连接到 FTP 服务器: {}:{}", ftpConfig.getHost(), ftpConfig.getPort());
    }

    private void disconnect(FTPClient ftpClient) {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
                log.info("FTP 连接已断开");
            }
        } catch (IOException e) {
            log.warn("断开 FTP 连接失败: {}", e.getMessage(), e);
        }
    }
}
