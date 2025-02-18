package com.ruoyi.fms.service;

import com.ruoyi.fms.config.FtpConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;    
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;

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
                    // 如果上传成功，额外输出可下载链接
                    String ftpUrl = getFtpUrl(remoteFolder, remoteFileName);
                    if (ftpUrl != null) {
                        log.info("可下载文件的 URL: {}", ftpUrl);
                    }
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
     * 处理文件流输出，用于下载或在线预览文件
     *
     * @param remoteFolder 文件所在远程目录（例如 "uploads/合格证"）
     * @param fileName     文件名（例如 "file.txt"）
     * @param isPreview    是否在线预览（true：预览，用 inline ；false：下载，用 attachment）
     * @param response     HttpServletResponse 用于输出文件流
     */
    public void streamFile(String remoteFolder, String fileName, boolean isPreview, HttpServletResponse response) {
        // 拼接本地临时文件路径（确保 ftpConfig.getTempDir() 已正确配置路径）
        String localFilePath = getTempDir() + fileName;
        try {
            // 调用已有的 FTP 下载方法，从 FTP 服务器下载文件到本地
            boolean success = downloadFile(remoteFolder, fileName, localFilePath);
            if (!success) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件下载失败或不存在");
                return;
            }
            // 根据模式设置响应头
            if (isPreview) {
                // 根据扩展名设置 Content-Type（实际项目中可使用更完整的解析方法）
                String contentType = "application/octet-stream";
                String lowerName = fileName.toLowerCase();
                if (lowerName.endsWith(".png")) {
                    contentType = "image/png";
                } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (lowerName.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (lowerName.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (lowerName.endsWith(".txt")) {
                    contentType = "text/plain";
                }
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "inline; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            } else {
                // 下载模式
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            }
            // 将本地文件流写入 HTTP 响应
            try (InputStream is = Files.newInputStream(Paths.get(localFilePath));
                 OutputStream os = response.getOutputStream()) {
                StreamUtils.copy(is, os);
            }
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                String mode = isPreview ? "预览" : "下载";
                response.getWriter().write(mode + "出现异常: " + e.getMessage());
                log.error(mode + "过程中异常", e);
            } catch (IOException ex) {
                log.error("返回异常信息时出现错误", ex);
            }
        } finally {
            // 清理本地临时文件
            try {
                Files.deleteIfExists(Paths.get(localFilePath));
            } catch (IOException ex) {
                log.warn("删除临时文件失败: " + localFilePath, ex);
            }
        }
    }   
    /**
     * 下载文件从 FTP 服务器。
     *
     * @param remoteFolder   远程文件夹路径（例如 "uploads/合格证"，注意该路径相对于 ftpConfig.getRemoteDir() 的实际映射路径）
     * @param remoteFileName 远程文件名（例如 "file.txt"）
     * @param localFilePath  本地文件保存的完整路径（例如 "/tmp/file.txt"）
     * @return 如果下载成功，返回 true；否则返回 false
     * @throws IOException 如果发生 I/O 错误
     */
    public boolean downloadFile(String remoteFolder, String remoteFileName, String localFilePath) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            // 1. 连接并登录到 FTP 服务器
            connectAndLogin(ftpClient);

            // 2. 设置文件传输为二进制方式，避免文本文件与二进制文件的传输差异
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // 3. 切换为被动模式，适应网络防火墙等环境
            ftpClient.enterLocalPassiveMode();

            // 4. 启用 UTF-8 编码（若服务器支持），确保中文和特殊字符正常传输
            enableUTF8Encoding(ftpClient);

            // 5. 切换到目标远程文件夹
            //    remoteFolder 是相对路径（例如 "uploads/合格证"），应与 FTP 服务器上实际路径对应
            boolean changedDir = ftpClient.changeWorkingDirectory(remoteFolder);
            if (!changedDir) {
                log.error("无法切换到远程目录: {}，回复码: {}, 回复信息: {}",
                        remoteFolder, ftpClient.getReplyCode(), ftpClient.getReplyString());
                return false;
            }

            // 6. 打开本地文件输出流，用于保存下载的文件数据
            File localFile = new File(localFilePath);
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFile))) {
                // 7. 从 FTP 服务器获取文件，并将内容写入本地输出流
                boolean success = ftpClient.retrieveFile(remoteFileName, outputStream);
                if (success) {
                    log.info("文件下载成功: {}/{} 到本地路径: {}", remoteFolder, remoteFileName, localFilePath);
                } else {
                    log.error("文件下载失败: {}/{}. 回复码: {}, 回复信息: {}",
                            remoteFolder, remoteFileName, ftpClient.getReplyCode(), ftpClient.getReplyString());
                }
                return success;
            }
        } catch (IOException e) {
            log.error("下载文件时发生异常: {}", e.getMessage(), e);
            throw e;
        } finally {
            // 8. 无论下载是否成功，都需要断开与 FTP 服务器的连接
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
     * 构建文件的 FTP 相对路径。
     *
     * @param remoteFolderPath 远程文件夹路径（例如 "uploads/合格证"）
     * @param remoteFileName   远程文件名
     * @return 文件的 FTP 相对路径（例如 "uploads/合格证/file.txt"）
     */
    public String getFtpUrl(String remoteFolderPath, String remoteFileName) {
        try {
            // 对路径中的空格进行简单处理（也可以根据需要进行更严格的编码）
            String encodedPath = remoteFolderPath.replace(" ", "%20")
                    + "/" + remoteFileName.replace(" ", "%20");
            return encodedPath;
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
