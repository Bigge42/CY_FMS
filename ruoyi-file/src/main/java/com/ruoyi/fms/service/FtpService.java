package com.ruoyi.fms.service;

import com.ruoyi.fms.config.FtpConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * FtpService 负责处理与 FTP 服务器的连接、上传、下载和删除文件操作。
 */
@Service
public class FtpService {

    private static final Logger log = LoggerFactory.getLogger(FtpService.class);

    @Autowired
    private FtpConfig ftpConfig;

    /**
     * 递归创建远程目录（逐级切换 & 创建）。
     *
     * @param ftpClient     FTPClient 实例
     * @param remoteDirPath 远程目录路径（相对于 ftp.remoteDir），例如 "uploads/abc/def"
     * @return 如果目录创建成功或已存在，返回 true；否则返回 false
     * @throws IOException 如果发生 I/O 错误
     */
    private boolean createDirectories(FTPClient ftpClient, String remoteDirPath) throws IOException {
        // 按 "/" 拆分目录层级
        String[] dirs = remoteDirPath.split("/");
        for (String dir : dirs) {
            // 跳过空字符串，避免多个 "/" 导致的空项
            if (dir == null || dir.trim().isEmpty()) {
                continue;
            }

            // 尝试切换到当前目录
            if (!ftpClient.changeWorkingDirectory(dir)) {
                // 切换失败，说明目录不存在，尝试创建
                boolean dirCreated = ftpClient.makeDirectory(dir);
                if (!dirCreated) {
                    log.error("无法创建远程目录: {}，回复码: {}, 回复信息: {}",
                            dir, ftpClient.getReplyCode(), ftpClient.getReplyString());
                    return false;
                }
                // 创建成功后，再尝试切换到该目录
                if (!ftpClient.changeWorkingDirectory(dir)) {
                    log.error("创建远程目录后无法切换到目录: {}，回复码: {}, 回复信息: {}",
                            dir, ftpClient.getReplyCode(), ftpClient.getReplyString());
                    return false;
                }
                log.info("成功创建并切换到远程目录: {}", dir);
            } else {
                log.info("远程目录已存在，且已切换到: {}", dir);
            }
        }
        return true;
    }

    /**
     * 上传文件到 FTP 服务器。
     *
     * @param localFilePath  本地文件路径
     * @param remoteFolder   远程文件夹路径（相对于 ftp.remoteDir）
     * @param remoteFileName 远程文件名
     * @return 如果上传成功，返回 true；否则返回 false
     * @throws IOException 如果发生 I/O 错误
     */
    public boolean uploadFile(String localFilePath, String remoteFolder, String remoteFileName) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            connectAndLogin(ftpClient);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            // 启用 UTF-8 编码
            enableUTF8Encoding(ftpClient);

            // 递归创建远程目录，逐级切换到目标目录
            if (!createDirectories(ftpClient, remoteFolder)) {
                throw new IOException("无法创建远程文件夹: " + remoteFolder);
            }

            // 此时当前工作目录即为 remoteFolder 的最后一级
            // 上传文件
            File localFile = new File(localFilePath);
            try (InputStream inputStream = new FileInputStream(localFile)) {
                boolean success = ftpClient.storeFile(remoteFileName, inputStream);
                if (success) {
                    log.info("文件上传成功: {}/{}", remoteFolder, remoteFileName);
                } else {
                    log.error("文件上传失败: {}/{}. 回复码: {}, 回复信息: {}",
                            remoteFolder, remoteFileName, ftpClient.getReplyCode(), ftpClient.getReplyString());
                }
                return success;
            }

        } finally {
            disconnect(ftpClient);
        }
    }

    /**
     * 下载文件从 FTP 服务器。
     *
     * @param remoteFolder   远程文件夹路径（相对于 ftp.remoteDir）
     * @param remoteFileName 远程文件名
     * @param localFilePath  本地文件路径，用于保存下载的文件
     * @return 如果下载成功，返回 true；否则返回 false
     * @throws IOException 如果发生 I/O 错误
     */
    public boolean downloadFile(String remoteFolder, String remoteFileName, String localFilePath) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            connectAndLogin(ftpClient);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            // 启用 UTF-8 编码
            enableUTF8Encoding(ftpClient);

            // 切换到目标目录
            boolean changedDir = ftpClient.changeWorkingDirectory(remoteFolder);
            if (!changedDir) {
                throw new IOException("无法切换到远程目录: " + remoteFolder);
            }

            // 下载文件
            File localFile = new File(localFilePath);
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFile))) {
                boolean success = ftpClient.retrieveFile(remoteFileName, outputStream);
                if (success) {
                    log.info("文件下载成功: {}/{} 到 {}", remoteFolder, remoteFileName, localFilePath);
                } else {
                    log.error("文件下载失败: {}/{}. 回复码: {}, 回复信息: {}",
                            remoteFolder, remoteFileName, ftpClient.getReplyCode(), ftpClient.getReplyString());
                }
                return success;
            }

        } finally {
            disconnect(ftpClient);
        }
    }

    /**
     * 删除文件从 FTP 服务器。
     *
     * @param remoteFolder   远程文件夹路径（相对于 ftp.remoteDir）
     * @param remoteFileName 远程文件名
     * @return 如果删除成功，返回 true；否则返回 false
     * @throws IOException 如果发生 I/O 错误
     */
    public boolean deleteFile(String remoteFolder, String remoteFileName) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            connectAndLogin(ftpClient);
            ftpClient.enterLocalPassiveMode();

            // 启用 UTF-8 编码
            enableUTF8Encoding(ftpClient);

            // 切换到目标目录
            boolean changedDir = ftpClient.changeWorkingDirectory(remoteFolder);
            if (!changedDir) {
                throw new IOException("无法切换到远程目录: " + remoteFolder);
            }

            // 删除文件
            boolean success = ftpClient.deleteFile(remoteFileName);
            if (success) {
                log.info("文件删除成功: {}/{}", remoteFolder, remoteFileName);
            } else {
                log.error("文件删除失败: {}/{}. 回复码: {}, 回复信息: {}",
                        remoteFolder, remoteFileName, ftpClient.getReplyCode(), ftpClient.getReplyString());
            }
            return success;

        } finally {
            disconnect(ftpClient);
        }
    }

    /**
     * 连接并登录到 FTP 服务器。
     *
     * @param ftpClient FTPClient 实例
     * @throws IOException 如果连接或登录失败
     */
    private void connectAndLogin(FTPClient ftpClient) throws IOException {
        try {
            ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new IOException("FTP 服务器拒绝连接。回复码: " + replyCode);
            }

            boolean loggedIn = ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            if (!loggedIn) {
                throw new IOException("FTP 登录失败，请检查用户名和密码。");
            }

            log.info("成功连接并登录到 FTP 服务器: {}:{}", ftpConfig.getHost(), ftpConfig.getPort());

            // 设置文件类型为二进制
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // 进入被动模式
            ftpClient.enterLocalPassiveMode();

            // 启用 UTF-8 编码
            enableUTF8Encoding(ftpClient);

        } catch (IOException e) {
            log.error("连接或登录到 FTP 服务器时发生错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 启用 UTF-8 编码，如果 FTP 服务器支持。
     *
     * @param ftpClient FTPClient 实例
     */
    private void enableUTF8Encoding(FTPClient ftpClient) {
        try {
            int replyCode = ftpClient.sendCommand("OPTS", "UTF8 ON");
            if (replyCode == 200) {
                log.info("FTP 服务器支持 UTF-8 编码，并已启用。");
                ftpClient.setControlEncoding("UTF-8");
            } else {
                log.warn("FTP 服务器不支持 UTF-8 编码。当前编码: {}", ftpClient.getControlEncoding());
            }
        } catch (IOException e) {
            log.warn("发送 UTF8 ON 命令失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 断开与 FTP 服务器的连接。
     *
     * @param ftpClient FTPClient 实例
     */
    private void disconnect(FTPClient ftpClient) {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
                log.info("已断开与 FTP 服务器的连接。");
            } catch (IOException e) {
                log.warn("断开与 FTP 服务器的连接时发生错误: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 获取远程文件夹的完整路径。
     *
     * @param folderPath 具体文件夹路径（例如 "合格证"）
     * @return 完整的远程文件夹路径（例如 "uploads/合格证"）
     */
    public String getRemoteFolderPath(String folderPath) {
        if (ftpConfig.getRemoteDir().endsWith("/")) {
            return ftpConfig.getRemoteDir() + folderPath;
        } else {
            return ftpConfig.getRemoteDir() + "/" + folderPath;
        }
    }

    /**
     * 构建文件的 FTP URL。
     *
     * @param remoteFolderPath 远程文件夹路径（例如 "uploads/合格证"）
     * @param remoteFileName   远程文件名
     * @return 文件的 FTP URL（例如 "ftp://username:password@host:port/uploads/合格证/file.txt"）
     */
    public String getFtpUrl(String remoteFolderPath, String remoteFileName) {
        try {
            String encodedPath = remoteFolderPath.replace(" ", "%20")
                    + "/" + remoteFileName.replace(" ", "%20");
            return String.format("ftp://%s:%s@%s:%d/%s",
                    ftpConfig.getUsername(),
                    ftpConfig.getPassword(),
                    ftpConfig.getHost(),
                    ftpConfig.getPort(),
                    encodedPath);
        } catch (Exception e) {
            log.error("构建 FTP URL 失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取临时目录路径，用于在本地存储上传或下载的文件。
     *
     * @return 临时目录路径
     */
    public String getTempDir() {
        return ftpConfig.getTempDir();
    }
}
