package com.ruoyi.fms.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.fms.domain.CYFile;
import com.ruoyi.fms.domain.CYFolder;
import com.ruoyi.fms.service.FileService;
import com.ruoyi.fms.service.FolderService;
import com.ruoyi.fms.service.FtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/fms/ftp")
public class FtpController {

    private static final Logger log = LoggerFactory.getLogger(FtpController.class);

    @Autowired
    private FtpService ftpService;

    @Autowired
    private FileService fileService;

    @Autowired
    private FolderService folderService;

    // 允许的文档类型
    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "Material Physicochemical Report",     // 材质理化报告
            "Certificate",                         // 合格证
            "Manual",                              // 说明书
            "Product Inspection Report",           // 产品检验报告
            "Packing List",                        // 装箱单
            "Supplier Raw Material Report",        // 供应商原材料报告
            "Packing Photo"                        // 装箱单照片
    );


    /**
     * 根据 DocumentTypeID 获取对应的文档类型名称（英文名称）
     *
     * @param documentTypeID 文档类型ID
     * @return 英文文档类型名称，如果不存在则返回 null
     */
    private String getDocumentTypeName(Integer documentTypeID) {
        if (documentTypeID == null) {
            return null;
        }
        switch (documentTypeID) {
            case 1:
                return "Material Physicochemical Report";  // 材质理化报告
            case 2:
                return "Certificate";                        // 合格证
            case 3:
                return "Manual";                             // 说明书
            case 4:
                return "Product Inspection Report";          // 产品检验报告
            case 5:
                return "Packing List";                       // 装箱单
            case 6:
                return "Supplier Raw Material Report";       // 供应商原材料报告
            case 7:
                return "Packing Photo";                      // 装箱单照片
            default:
                return null;
        }
    }

    /**
     * 文件上传接口
     *
     * @param file               上传的文件
     * @param documentTypeID     文档类型ID（必填）
     * @param matchID            匹配ID（必填）
     * @param planTrackingNumber 计划跟踪号（选填）
     * @return 上传结果
     */
    @Anonymous
    @PostMapping("/upload")
    public Response uploadFile(@RequestParam("file") MultipartFile file,
                               @RequestParam("DocumentTypeID") Integer documentTypeID,
                               @RequestParam("matchID") String matchID,
                               @RequestParam(value = "PlanTrackingNumber", required = false) String planTrackingNumber) {

        if (documentTypeID == null) {
            log.warn("DocumentTypeID 不能为空");
            return Response.error("DocumentTypeID 不能为空");
        }

        if (file.isEmpty()) {
            log.warn("上传的文件为空");
            return Response.error("上传的文件为空");
        }

        if (matchID == null) {
            log.warn("MatchID 不能为空");
            return Response.error("MatchID 不能为空");
        }
        // 根据 DocumentTypeID 获取文档类型名称（英文名称）
        String documentTypeName = getDocumentTypeName(documentTypeID);
        if (documentTypeName == null || !ALLOWED_DOCUMENT_TYPES.contains(documentTypeName)) {
            log.warn("不支持的文档类型ID: {}", documentTypeID);
            return Response.error("不支持的文档类型ID: " + documentTypeID);
        }

        // 生成时间戳
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        // 原始文件名
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            originalFileName = "unknown";
        }

        // 文件扩展名
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex != -1) {
            extension = originalFileName.substring(dotIndex);
            originalFileName = originalFileName.substring(0, dotIndex);
        }

        // 新文件名添加时间戳
        String newFileName = originalFileName + "_" + timestamp + extension;

        String localFilePath = ftpService.getTempDir() + newFileName;
        try {
            // 创建临时目录（如果不存在）
            File tempDirectory = new File(ftpService.getTempDir());
            if (!tempDirectory.exists()) {
                boolean dirsCreated = tempDirectory.mkdirs();
                if (dirsCreated) {
                    log.info("临时目录已创建: {}", ftpService.getTempDir());
                } else {
                    log.warn("无法创建临时目录: {}", ftpService.getTempDir());
                    return Response.error("无法创建临时目录: " + ftpService.getTempDir());
                }
            }

            // 保存文件到本地
            file.transferTo(new File(localFilePath));
            log.info("文件保存到本地临时路径: {}", localFilePath);

            // 查找或创建文件夹
            CYFolder folder = folderService.findOrCreateFolder(documentTypeName, "system"); // 可以根据实际情况替换创建者
            String remoteFolderPath = ftpService.getRemoteFolderPath(folder.getPhysicalPath());

            // 上传文件到 FTP
            boolean uploadResult = ftpService.uploadFile(localFilePath, remoteFolderPath, newFileName);
            if (!uploadResult) {
                log.error("文件上传到 FTP 失败: {}", newFileName);
                return Response.error("文件上传到 FTP 失败");
            }
            log.info("文件成功上传到 FTP: {}", newFileName);

            // 构建文件URL
            String fileURL = ftpService.getFtpUrl(remoteFolderPath, newFileName);
            if (fileURL == null) {
                log.warn("构建文件 URL 失败");
                return Response.error("构建文件 URL 失败");
            }

            // 插入文件记录到数据库
            CYFile cyFile = new CYFile();
            cyFile.setFileName(originalFileName + extension);
            cyFile.setFolderID(folder.getFolderID());
            cyFile.setDocumentTypeName(documentTypeName);
            cyFile.setMatchID(matchID);
            cyFile.setVersionNumber(timestamp);
            cyFile.setCreatedBy("system"); // 可以根据实际情况替换创建者
            cyFile.setFileURL(fileURL);
            cyFile.setPlanTrackingNumber(planTrackingNumber);

            int insertResult = fileService.saveFileRecord(cyFile);
            if (insertResult <= 0) {
                log.warn("数据库插入文件记录失败");
                return Response.error("数据库插入文件记录失败");
            }
            log.info("文件记录已插入数据库: 文件名={}, 文件夹编码={}", cyFile.getFileName(), folder.getFolderCode());

            // 清理本地临时文件
            boolean deleted = new File(localFilePath).delete();
            if (deleted) {
                log.info("本地临时文件已删除: {}", localFilePath);
            } else {
                log.warn("本地临时文件删除失败: {}", localFilePath);
            }

            return Response.success("文件上传成功并已存储到数据库", fileURL);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return Response.error("文件上传失败: " + e.getMessage());
        }
    }


    /**
     * 下载文件接口 - 通过相对路径，直接返回文件流
     *
     * @param filePath 相对路径，如 "uploads/合格证/file.txt"
     * @param response HttpServletResponse，用于输出文件流
     */
    @Anonymous
    @GetMapping("/download")
    public void downloadFile(@RequestParam("filePath") String filePath,
                             HttpServletResponse response) {
        try {
            // 1. 分割出 folder & fileName
            int lastSlashIndex = filePath.lastIndexOf('/');
            if (lastSlashIndex < 0) {
                // 如果没有'/'，说明传入有误，这里可自行处理
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("filePath 格式不正确");
                return;
            }
            String folder = filePath.substring(0, lastSlashIndex);
            String fileName = filePath.substring(lastSlashIndex + 1);

            // 2. 本地暂存路径
            String localFilePath = ftpService.getTempDir() + fileName;

            // 3. 调用 FtpService 下载
            boolean success = ftpService.downloadFile(folder, fileName, localFilePath);
            if (!success) {
                // 下载失败就写一个提示
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件下载失败或不存在");
                return;
            }

            // 4. 设置响应头
            // Content-Type 根据文件后缀而定，简化处理成通用的application/octet-stream
            response.setContentType("application/octet-stream");
            // 设置下载弹窗的文件名，这里直接使用原始 fileName
            // 如果需要处理中文文件名等，请进行URL编码
            response.setHeader("Content-Disposition",
                    "attachment;filename=\"" + fileName + "\"");

            // 5. 将本地文件流写入 response 输出流
            try (java.io.InputStream is = Files.newInputStream(Paths.get(localFilePath))) {
                StreamUtils.copy(is, response.getOutputStream());
            }

            // 6. 如果需要，写完后清理本地缓存文件
            Files.deleteIfExists(Paths.get(localFilePath));

        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("下载出现异常: " + e.getMessage());
            } catch (Exception ex) {
                // 忽略写错误信息时的异常
            }
        }
    }

    /**
     * 浏览文件接口 - 通过相对路径，直接返回文件流
     *
     * @param filePath 相对路径，如 "uploads/合格证/file.txt"
     * @param response HttpServletResponse，用于输出文件流
     */
    @Anonymous
    @GetMapping("/preview")
    public void previewFile(@RequestParam("filePath") String filePath,
                            HttpServletResponse response) {
        try {
            // 1. 解析 filePath，分离出文件夹和文件名
            int lastSlashIndex = filePath.lastIndexOf('/');
            if (lastSlashIndex < 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("filePath 格式不正确");
                return;
            }
            String remoteFolder = filePath.substring(0, lastSlashIndex);
            String fileName = filePath.substring(lastSlashIndex + 1);

            // 2. 本地暂存路径（可以使用临时目录）
            String localFilePath = ftpService.getTempDir() + fileName;

            // 3. 下载文件到本地（与下载接口类似）
            boolean success = ftpService.downloadFile(remoteFolder, fileName, localFilePath);
            if (!success) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件下载失败或不存在");
                return;
            }

            // 4. 根据文件类型设置 Content-Type
            // 例如，这里简单根据文件扩展名决定，实际可使用更完善的方式。
            String contentType = "application/octet-stream";
            if (fileName.endsWith(".png")) {
                contentType = "image/png";
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (fileName.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (fileName.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (fileName.endsWith(".txt")) {
                contentType = "text/plain";
            }
            response.setContentType(contentType);

            // 5. 设置响应头，采用 inline 使浏览器直接显示，不弹出下载对话框
            response.setHeader("Content-Disposition", "inline; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 6. 将本地文件流写入 HTTP 响应
            try (InputStream inputStream = new FileInputStream(localFilePath);
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // 7. 删除本地临时文件（根据实际情况选择是否删除）
            new File(localFilePath).delete();
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("预览出现异常: " + e.getMessage());
            } catch (IOException ex) {
                log.error("无法返回异常信息到客户端", ex);
            }
        }
    }
    /**
     * 删除文件接口
     *
     * @param fileName 文档类型名称和文件名（用于查找文件记录）
     * @param matchID  匹配ID
     * @return 删除结果
     */
    @Anonymous
    @DeleteMapping("/delete")
    public Response deleteFile(@RequestParam("fileName") String fileName,
                               @RequestParam("matchID") Integer matchID) {
        try {
            // 根据文件名和 MatchID 查找文件记录
            CYFile cyFile = fileService.findFileByNameAndMatchID(fileName, matchID);
            if (cyFile == null) {
                log.warn("未找到对应的文件记录: 文件名={}, MatchID={}", fileName, matchID);
                return Response.error("未找到对应的文件记录");
            }

            // 获取文件夹信息
            CYFolder folder = folderService.findFolderById(cyFile.getFolderID());
            if (folder == null) {
                log.warn("未找到对应的文件夹: FolderID={}", cyFile.getFolderID());
                return Response.error("未找到对应的文件夹");
            }

            String remoteFolderPath = ftpService.getRemoteFolderPath(folder.getPhysicalPath());
            String remoteFileName = cyFile.getFileName();

            // 删除文件从 FTP
            boolean deleteResult = ftpService.deleteFile(remoteFolderPath, remoteFileName);
            if (!deleteResult) {
                log.error("文件删除失败: {}", remoteFileName);
                return Response.error("文件删除失败");
            }
            log.info("文件成功从 FTP 删除: {}", remoteFileName);

            // 标记数据库记录为删除
            int updateResult = fileService.markFileAsDeleted(cyFile.getFileID());
            if (updateResult <= 0) {
                log.warn("数据库标记文件记录为删除失败: FileID={}", cyFile.getFileID());
                return Response.error("数据库标记文件记录为删除失败");
            }
            log.info("文件记录已标记为删除: FileID={}", cyFile.getFileID());

            return Response.success("文件删除成功");
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return Response.error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 标准化响应类
     */
    public static class Response {
        private int code;
        private String msg;
        private String url;

        public Response(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Response(int code, String msg, String url) {
            this.code = code;
            this.msg = msg;
            this.url = url;
        }

        public static Response success(String msg,String url) {
            return new Response(200, msg,url);
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

        public String getUrl() {
            return url;
        }
    }
}
