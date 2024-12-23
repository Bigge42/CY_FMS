package com.ruoyi.fms.service;

import com.ruoyi.fms.config.FtpConfig;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class FtpService {

    @Autowired
    private FtpConfig ftpConfig;

    // 上传文件到 FTP
    public boolean uploadFile(String localFilePath, String remoteFileName) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            // 连接到 FTP 服务器
            ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.changeWorkingDirectory(ftpConfig.getRemoteDir());

            // 上传文件
            File localFile = new File(localFilePath);
            try (FileInputStream fis = new FileInputStream(localFile)) {
                return ftpClient.storeFile(remoteFileName, fis);
            }
        } finally {
            ftpClient.logout();
            ftpClient.disconnect();
        }
    }

    // 从 FTP 下载文件
    public boolean downloadFile(String remoteFileName, String localFilePath) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            // 连接到 FTP 服务器
            ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.changeWorkingDirectory(ftpConfig.getRemoteDir());

            // 下载文件
            File localFile = new File(localFilePath);
            try (FileOutputStream fos = new FileOutputStream(localFile)) {
                return ftpClient.retrieveFile(remoteFileName, fos);
            }
        } finally {
            ftpClient.logout();
            ftpClient.disconnect();
        }
    }
}
