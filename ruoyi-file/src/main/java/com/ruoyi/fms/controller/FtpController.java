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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
            "Supplier Raw Material Report"         // 供应商原材料报告
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
     * 文件下载接口
     *
     * @param documentTypeName 文档类型名称和文件名（用于查找文件记录）
     * @param matchID  匹配ID
     * @return 下载结果
     */
    @Anonymous
    @GetMapping("/download")
    public Response downloadFile(@RequestParam("documentTypeName") String documentTypeName,
                                 @RequestParam("matchID") Integer matchID) {
        try {
            // 根据文件名和 MatchID 查找文件记录
            CYFile cyFile = fileService.findFileByNameAndMatchID(documentTypeName, matchID);
            if (cyFile == null) {
                log.warn("未找到对应的文件记录: 文件类型={}, MatchID={}", documentTypeName, matchID);
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

            // 下载文件到临时目录
            String localFilePath = ftpService.getTempDir() + remoteFileName;
            boolean downloadResult = ftpService.downloadFile(remoteFolderPath, remoteFileName, localFilePath);
            if (!downloadResult) {
                log.error("文件下载失败: {}", remoteFileName);
                return Response.error("文件下载失败");
            }
            log.info("文件成功从 FTP 下载: {}", remoteFileName);

            return Response.success("文件下载成功，保存到: " + localFilePath);
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            return Response.error("文件下载失败: " + e.getMessage());
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
