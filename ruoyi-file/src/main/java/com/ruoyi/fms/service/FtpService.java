package com.ruoyi.fms.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruoyi.fms.config.FtpConfig;
import com.ruoyi.fms.mapper.CYFileMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;    
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * FtpService 负责处理与 FTP 服务器的连接、上传、下载和删除文件操作。
 */
@Service
public class    FtpService {
    @Resource
    private FileService fileService;       // 查询 fileURL 的 Service
    @Resource
    private CYFileMapper fileMapper;       // 如果你直接用 mapper
    private static final Logger log = LoggerFactory.getLogger(FtpService.class);

    @Autowired
    private FtpConfig ftpConfig;


    /**
     * 递归创建远程目录（逐级切换 & 创建）
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
     * 打开 FTP 上某个文件的输入流，用于流式读取。
     *
     * @param remoteFolder 远程目录（相对于 ftpConfig.getRemoteDir()）
     * @param fileName     远程文件名
     * @return 对应文件的 InputStream
     * @throws IOException 登录、切目录或拉流失败时抛出
     */
    public InputStream retrieveFileStream(String remoteFolder, String fileName) throws IOException {
        FTPClient ftpClient = new FTPClient();
        // 1. 连接并登录
        connectAndLogin(ftpClient);
        // 2. 二进制模式、被动模式、UTF-8
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();
        enableUTF8Encoding(ftpClient);
        // 3. 切换到目录
        if (!ftpClient.changeWorkingDirectory(remoteFolder)) {
            throw new IOException("切换目录失败: " + remoteFolder + "，replyCode=" + ftpClient.getReplyCode());
        }
        // 4. 打开流
        return ftpClient.retrieveFileStream(fileName);
    }


    /**
     * 结束上一次 retrieveFileStream 操作，并断开连接。
     *
     * @param ftpClient 上一个方法内部 new 出来的 FTPClient
     * @return 完成命令是否成功
     * @throws IOException I/O 异常
     */
    public boolean completePendingCommand(FTPClient ftpClient) throws IOException {
        try {
            return ftpClient.completePendingCommand();
        } finally {
            // 退出并断开
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
            // 对文件名进行 URL 编码，空格替换为 '+'
            String encodedFileName = remoteFileName.replace(" ", "+"); // 将空格替换为 '+'

            // 返回组合后的路径，文件夹路径保持不变，只对文件名部分进行处理
            return remoteFolderPath + "/" + encodedFileName;
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


    public void writeFilesToZip(List<String> fileIds, OutputStream out) throws IOException {
        // 0. 拿到所有 fileURL
        List<String> paths = fileService.getFilePathsByFileIds(fileIds);

        try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
            for (String filePath : paths) {
                if (filePath == null) continue;

                // 拆目录和文件名
                int idx = filePath.lastIndexOf('/');
                String remoteFolder = filePath.substring(0, idx);
                String fileName = filePath.substring(idx + 1);

                // 1) 打开 FTP 连接并登录
                FTPClient ftp = new FTPClient();
                connectAndLogin(ftp);
                ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftp.enterLocalPassiveMode();
                enableUTF8Encoding(ftp);

                // 2) 切目录
                if (!ftp.changeWorkingDirectory(remoteFolder)) {
                    disconnect(ftp);
                    continue;
                }

                // 3) 拉流、写 ZIP entry
                try (InputStream is = ftp.retrieveFileStream(fileName)) {
                    zipOut.putNextEntry(new ZipEntry(fileName));
                    IOUtils.copy(is, zipOut);
                    zipOut.closeEntry();
                }

                // 4) 完成命令并断开
                ftp.completePendingCommand();
                disconnect(ftp);
            }
            zipOut.finish();
        }
    }

    /**
     * 文件元数据：远程目录 + 文件名
     */
    public static class FileMeta {
        public final String remoteDir;
        public final String fileName;
        public final Date lastModified;  // 新增

        public FileMeta(String remoteDir, String fileName, Date lastModified) {
            this.remoteDir = remoteDir;
            this.fileName = fileName;
            this.lastModified = lastModified;
        }
    }


    /**
     * TCListFilesByCode：根据物料编码列出所有可下载文件
     */
    public List<FileMeta> TCListFilesByCode(String code) {
        String sanitized = code.replace("/", "#").replace("*", "星");
        FTPClient ftp = new FTPClient();
        ftp.setControlEncoding("GBK");
        FTPClientConfig cfg = new FTPClientConfig(FTPClientConfig.SYST_NT);
        cfg.setServerLanguageCode("zh");
        ftp.configure(cfg);

        List<FileMeta> result = new ArrayList<>();
        try {
            ftp.connect(ftpConfig.getHost(), ftpConfig.getPort());
            if (!ftp.login(ftpConfig.getUsername(), ftpConfig.getPassword())) {
                throw new RuntimeException("FTP 登录失败");
            }
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);


            boolean hasDotOrDash = sanitized.contains(".") || sanitized.contains("-");
            boolean hasPCode = sanitized.matches("(?i).*P\\d{10}.*");

            if (hasDotOrDash || hasPCode) {
                // SMT 路径
                String dir = "/SMT/smtpdf";
                if (ftp.changeWorkingDirectory(dir)) {
                    for (FTPFile f : ftp.listFiles()) {
                        if (!f.isDirectory() && f.getName().equals(sanitized + ".pdf")) {
                            Date lm = f.getTimestamp().getTime();
                            result.add(new FileMeta(dir, f.getName(), lm));
                            break;
                        }
                    }
                }
            } else {
                // TZ 路径
                String base = "/TZ/TZ";
                if (!ftp.changeWorkingDirectory(base)) {
                    throw new RuntimeException("切换基础目录失败：" + base);
                }
                String prefix = sanitized + "&";
                // 找子目录
                String sel = Arrays.stream(ftp.listDirectories())
                        .map(FTPFile::getName)
                        .filter(n -> n.startsWith(prefix))
                        .max(Comparator.comparing(n -> n.substring(prefix.length())))
                        .orElseThrow(() -> new RuntimeException("未找到 TZ 子目录：" + code));

                String fullDir = base + "/" + sel;
                if (!ftp.changeWorkingDirectory(fullDir)) {
                    throw new RuntimeException("切换到目标目录失败：" + fullDir);
                }
                for (FTPFile f : ftp.listFiles()) {
                    if (!f.isDirectory()) {
                        Date lm = f.getTimestamp().getTime();
                        result.add(new FileMeta(fullDir, f.getName(), lm));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("列文件失败: " + e.getMessage(), e);
        } finally {
            TCDisconnect(ftp);
        }
        return result;
    }


    public void TCDownload(String fullPath, HttpServletResponse response) {
        // 分离目录和文件名
        int idx = fullPath.lastIndexOf('/');
        if (idx <= 0 || idx == fullPath.length() - 1) {
            throw new IllegalArgumentException("无效的 path 参数: " + fullPath);
        }
        String remoteDir = fullPath.substring(0, idx);
        String fileName = fullPath.substring(idx + 1);
        // 调用原有逻辑
        TCDownload(remoteDir, fileName, response);
    }

    /**
     * TCDownload：根据 remoteDir 和 fileName 拉取并返回文件
     */
    public void TCDownload(String remoteDir, String fileName, HttpServletResponse response) {
        FTPClient ftp = new FTPClient();
        ftp.setControlEncoding("GBK");
        FTPClientConfig cfg = new FTPClientConfig(FTPClientConfig.SYST_NT);
        cfg.setServerLanguageCode("zh");
        ftp.configure(cfg);

        try {
            ftp.connect(ftpConfig.getHost(), ftpConfig.getPort());
            if (!ftp.login(ftpConfig.getUsername(), ftpConfig.getPassword())) {
                throw new RuntimeException("FTP 登录失败");
            }
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            if (!ftp.changeWorkingDirectory(remoteDir) ||
                    Arrays.stream(ftp.listFiles()).noneMatch(f -> f.getName().equals(fileName))) {
                response.reset();
                throw new RuntimeException("文件不存在：" + fileName);
            }

            // 构造下载头
            String pct = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
            String fallback = new String(
                    fileName.getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.ISO_8859_1
            );
            String cd = String.format(
                    "inline; filename=\"%s\"; filename*=UTF-8''%s",//"attachment; filename=\"%s\"; filename*=UTF-8''%s"
                    fallback, pct
            );
            response.reset();
            response.setHeader("Content-Disposition", cd);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/octet-stream");

            // 拉取文件
            try (OutputStream os = response.getOutputStream()) {
                if (!ftp.retrieveFile(fileName, os)) {
                    throw new RuntimeException("FTP retrieveFile 返回 false");
                }
            }
        } catch (IOException e) {
            response.reset();
            throw new RuntimeException("FTP 下载异常: " + e.getMessage(), e);
        } finally {
            TCDisconnect(ftp);
        }
    }

    /**
     * TCExistsOnFtp：检查指定目录下是否存在指定文件
     */
    private boolean TCExistsOnFtp(FTPClient ftp, String dir, String fn) throws IOException {
        if (!ftp.changeWorkingDirectory(dir)) return false;
        return Arrays.stream(ftp.listFiles())
                .map(FTPFile::getName)
                .anyMatch(n -> n.equals(fn));
    }

    /**
     * TCDisconnect：安全断开 FTP 连接
     */
    private void TCDisconnect(FTPClient ftp) {
        if (ftp.isConnected()) {
            try {
                ftp.logout();
                ftp.disconnect();
            } catch (IOException ignored) {
            }
        }
    }


    /**
     * FTP Service，负责构造文件下载 URL 并返回给前端
     */


        /**
         * DTO：单条下载信息，包含 URL 和文件最后修改时间
         */
        public static class FileUrlInfo {
            private String url;
            private String lastModified;

            public FileUrlInfo(String url, String lastModified) {
                this.url = url;
                this.lastModified = lastModified;
            }

            public String getUrl() {
                return url;
            }

            public String getLastModified() {
                return lastModified;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public void setLastModified(String lm) {
                this.lastModified = lm;
            }
        }

        /**
         * DTO：封装前端返回结构，只保留单个 url 和 BGurl 对象
         */
        public static class FileUrlResponse {
            private String materialCode;
            private FileUrlInfo url;            // 普通下载信息
            @JsonProperty("BGurl")
            private FileUrlInfo bgurl;          // 含 “BG” 文件的下载信息

            public FileUrlResponse(String materialCode) {
                this.materialCode = materialCode;
            }

            public String getMaterialCode() {
                return materialCode;
            }

            public FileUrlInfo getUrl() {
                return url;
            }

            public FileUrlInfo getBgurl() {
                return bgurl;
            }

            public void setMaterialCode(String mc) {
                this.materialCode = mc;
            }

            public void setUrl(FileUrlInfo u) {
                this.url = u;
            }

            public void setBgurl(FileUrlInfo b) {
                this.bgurl = b;
            }
        }

        /**
         * 根据物料编码和 baseUrl 构造下载列表
         * 仅返回第一条普通文件和第一条 BG 文件的信息
         */
        public FileUrlResponse TCBuildFileUrls(String code, String baseUrl)
                throws UnsupportedEncodingException {
            // 1. 列出所有符合的远程文件，FileMeta 中已包含 lastModified
            List<FileMeta> metas = TCListFilesByCode(code);

            // 2. 构造返回 DTO，对应 materialCode
            FileUrlResponse resp = new FileUrlResponse(code);

            // 3. 时间格式化器："yyyy-MM-dd HH:mm:ss"
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 4. 遍历所有文件，按需取第一条 url 和 BGurl
            for (FileMeta meta : metas) {
                // 4.1 原始路径 + URL 编码
                String rawPath = meta.remoteDir + "/" + meta.fileName;
                String encodedPath = URLEncoder.encode(rawPath, StandardCharsets.UTF_8.name());

                // 4.2 构造下载链接
                String downloadUrl = UriComponentsBuilder
                        .fromHttpUrl(baseUrl)
                        .path("/fms/ftp/TCdownload")
                        .queryParam("path", encodedPath)
                        .build()
                        .toUriString();

                // 4.3 格式化最后修改时间
                String lm = fmt.format(meta.lastModified);

                // 4.4 封装单条下载信息
                FileUrlInfo info = new FileUrlInfo(downloadUrl, lm);

                // 4.5 根据文件名分流
                if (meta.fileName.contains("BG")) {
                    // 仅填充第一条 BGurl
                    if (resp.getBgurl() == null) {
                        resp.setBgurl(info);
                    }
                } else {
                    // 仅填充第一条普通 url
                    if (resp.getUrl() == null) {
                        resp.setUrl(info);
                    }
                }

                // 如果两者都已填，提前退出
                if (resp.getUrl() != null && resp.getBgurl() != null) {
                    break;
                }
            }

            return resp;
        }


    /**
     * 上传并重命名（覆盖同名）：
     * 1. 使用 ASCII 临时名上传
     * 2. 删除旧文件
     * 3. 重命名为中文原名（GBK->ISO-8859-1）
     *
     * @param localFilePath 本地文件绝对路径
     * @param subFolder     目标子目录（不含前导 '/']
     * @param originalName  原始文件名（含中文/空格）
     * @return true 成功，否则 false
     */
    /**
     * 上传并重命名：
     * 1. 使用纯 ASCII 临时名上传
     * 2. 为同名文件自动添加后缀（_A、_B...）
     * 3. 同一会话中，用 GBK 控制通道重命名为中文原名或带后缀的目标名
     *
     * @param localFilePath 本地文件绝对路径
     * @param subFolder     目标子目录（不含前导 '/'）
     * @param originalName  原始文件名（含中文/空格）
     * @return true 成功，否则 false
     */
    public boolean uploadThenRenameByListing(String localFilePath,
                                             String subFolder,
                                             String originalName) {
        FTPClient ftp = new FTPClient();
        try {
            // 1. 连接并登录
            connectAndLogin(ftp);

            // 2. 控制通道使用 GBK (关闭 UTF-8)
            ftp.setControlEncoding("GBK");
            ftp.sendCommand("OPTS UTF8", "OFF");
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();

            // 3. 确保并进入目标目录
            mkdirsSilent(ftp, subFolder);
            if (!ftp.changeWorkingDirectory(subFolder)) {
                log.error("切换目录失败：{}", subFolder);
                return false;
            }

            // 4. 上传临时文件 (纯 ASCII 名)
            String tempName = UUID.randomUUID().toString() + ".tmp";
            try (InputStream in = new FileInputStream(localFilePath)) {
                if (!ftp.storeFile(tempName, in)) {
                    log.error("临时文件上传失败：{}", ftp.getReplyString());
                    return false;
                }
            }

            // 可选：打印目录确认
            String[] names = ftp.listNames();
            Set<String> existing = new HashSet<>(Arrays.asList(names));
            for (String name : existing) {
                log.info("[服务器文件] {}", name);
            }

            // 5. 生成目标文件名，若已存在则追加后缀 _A、_B...
            String targetName = originalName;
            if (existing.contains(targetName)) {
                String base = originalName;
                String ext = "";
                int dot = originalName.lastIndexOf('.');
                if (dot != -1) {
                    base = originalName.substring(0, dot);
                    ext = originalName.substring(dot);
                }
                char suffix = 'A';
                while (existing.contains(base + "_" + suffix + ext)) {
                    suffix++;
                }
                targetName = base + "_" + suffix + ext;
            }

            // 6. 重命名为目标名 (GBK->ISO-8859-1)
            String isoTarget = new String(targetName.getBytes("GBK"), "ISO-8859-1");
            if (!ftp.rename(tempName, isoTarget)) {
                log.error("重命名失败：{} -> {}，{}", tempName, targetName, ftp.getReplyString());
                return false;
            }

            return true;
        } catch (IOException e) {
            log.error("上传并重命名异常", e);
            return false;
        } finally {
            disconnect(ftp);
        }
    }


    /**
     * 递归创建远程目录：静默失败
     */
    private void mkdirsSilent(FTPClient ftp, String dirPath) throws IOException {
        String[] parts = dirPath.split("/");
        StringBuilder path = new StringBuilder();
        for (String p : parts) {
            path.append("/").append(p);
            ftp.makeDirectory(path.toString());
        }
    }
    /** 构造完整远程路径，去掉前导“/”并加上 base */
    private String buildRemotePath(String subFolder) {
        return subFolder.startsWith("/") ? subFolder.substring(1) : subFolder;
    }
}

