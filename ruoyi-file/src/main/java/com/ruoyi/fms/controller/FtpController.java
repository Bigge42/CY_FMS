package com.ruoyi.fms.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.fms.domain.CYFile;
import com.ruoyi.fms.domain.CYFolder;
import com.ruoyi.fms.service.FileService;
import com.ruoyi.fms.service.FolderService;
import com.ruoyi.fms.service.FtpService;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
            "Packing Photo",                       // 装箱单照片
            "Welding Report",                      // 焊接报告
            "Part Dimension Record Report",        // 零件尺寸记录报告
            "Heat Treatment Report",               // 热处理报告
            "Spraying Report",                     // 喷涂报告
            "Spray Welding Report"                 // 喷焊报告
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
            case 8:
                return "Welding Report";                     // 焊接报告
            case 9:
                return "Part Dimension Record Report";       // 零件尺寸记录报告
            case 10:
                return "Heat Treatment Report";              // 热处理报告
            case 11:
                return "Spraying Report";                    // 喷涂报告
            case 12:
                return "Spray Welding Report";               // 喷焊报告
            default:
                return null;
        }
    }

    /**
     * 直接上传文件接口，不进行转换
     *
     * @param file               上传的文件
     * @param documentTypeID     文档类型ID（必填）
     * @param matchID            匹配ID（必填）
     * @param planTrackingNumber 计划跟踪号（选填）
     * @param createdBy          创建人（必填）
     * @return 上传结果，返回文件ID
     */
    @Anonymous
    @PostMapping("/upload")
    public Response uploadFile(@RequestParam("file") MultipartFile file,
                               @RequestParam("DocumentTypeID") Integer documentTypeID,
                               @RequestParam("matchID") String matchID,
                               @RequestParam(value = "PlanTrackingNumber", required = false) String planTrackingNumber,
                               @RequestParam("createdBy") String createdBy) {
        return processFileUpload(file, documentTypeID, matchID, planTrackingNumber, false, createdBy);
    }

    /**
     * 上传文件后转换为 PDF 接口
     *
     * @param file               上传的文件（数据流）
     * @param documentTypeID     文档类型ID（必填）
     * @param matchID            匹配ID（必填）
     * @param planTrackingNumber 计划跟踪号（选填）
     * @param createdBy          创建人（必填）
     * @return 上传结果，返回文件ID
     */
    @Anonymous
    @PostMapping("/uploadToPdf")
    public Response uploadFileToPdf(@RequestParam("file") MultipartFile file,
                                    @RequestParam("DocumentTypeID") Integer documentTypeID,
                                    @RequestParam("matchID") String matchID,
                                    @RequestParam(value = "PlanTrackingNumber", required = false) String planTrackingNumber,
                                    @RequestParam("createdBy") String createdBy) {
        return processFileUpload(file, documentTypeID, matchID, planTrackingNumber, true, createdBy);
    }




    /**
     * 下载文件接口 - 附件下载模式
     *
     * @param fileId   文件ID，根据文件ID在数据库中查找对应的文件路径
     * @param response HttpServletResponse，用于输出文件流
     */
    @Anonymous
    @GetMapping("/download")
    public void downloadFile(@RequestParam("fileId") String fileId,
                             HttpServletResponse response) {
        // 根据 fileId 获取文件路径（例如从数据库中查询）
        String filePath = fileService.getFilePathByFileId(fileId);
        if(filePath == null) {
            // 文件路径为空，返回错误提示或设置响应状态码
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // 调用通用处理方法，false 表示附件下载
        processFile(filePath, response, false);
    }

    /**
     * 在线预览接口 - 内联预览模式
     *
     * @param fileId   文件ID，根据文件ID在数据库中查找对应的文件路径
     * @param response HttpServletResponse，用于输出文件流
     */
    @Anonymous
    @GetMapping("/preview")
    public void previewFile(@RequestParam("fileId") String fileId,
                            HttpServletResponse response) {
        // 根据 fileId 获取文件路径
        String filePath = fileService.getFilePathByFileId(fileId);
        if(filePath == null) {
            // 文件路径为空，返回错误提示或设置响应状态码
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // 调用通用处理方法，true 表示在线预览
        processFile(filePath, response, true);
    }



    @Anonymous
    @GetMapping("/getSmtFile")
    public void getSmtFile(@RequestParam("smtfile") String smtfile, HttpServletResponse response) {
        try {
            // 如果传入的 smtfile 不以 ".pdf"（不区分大小写）结尾，则追加 .pdf
            if (!smtfile.toLowerCase().endsWith(".pdf")) {
                // 如果文件名末尾是一个点，则去掉该点
                if (smtfile.endsWith(".")) {
                    smtfile = smtfile.substring(0, smtfile.length() - 1);
                }
                smtfile = smtfile + ".pdf";
            }

            // 固定 FTP 服务器的远程目录为 "/smtpdf"
            String remoteFolder = "/smtpdf";
            // 拼接本地临时文件路径，假设 ftpService.getTempDir() 返回类似 "/tmp/" 的路径
            String localFilePath = ftpService.getTempDir() + smtfile;

            // 调用服务层下载文件
            boolean success = ftpService.downloadFile(remoteFolder, smtfile, localFilePath);
            if (!success) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件下载失败或不存在");
                return;
            }

            // 设置响应头，采用附件方式返回文件流
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + smtfile + "\"");

            // 将本地临时文件的内容写入 HTTP 响应
            try (InputStream is = new FileInputStream(localFilePath);
                 OutputStream os = response.getOutputStream()) {
                StreamUtils.copy(is, os);
            }

            // 下载完成后删除本地临时文件
            Files.deleteIfExists(Paths.get(localFilePath));
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("下载出现异常: " + e.getMessage());
            } catch (IOException ex) {
                // 忽略写入异常信息时的错误
            }
        }
    }


    /**
     * 公共方法：解析请求参数并调用 Service 层流文件输出方法
     *
     * @param filePath  文件的相对路径
     * @param response  HttpServletResponse
     * @param isPreview 是否预览模式（true：在线预览；false：附件下载）
     */
    private void processFile(String filePath, HttpServletResponse response, boolean isPreview) {
        // 解析 filePath，提取 remoteFolder 和 fileName
        int lastSlashIndex = filePath.lastIndexOf('/');
        if (lastSlashIndex < 0) {
            try {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("filePath 格式不正确");
            } catch (Exception e) {
                log.error("处理错误响应时异常", e);
            }
            return;
        }
        String remoteFolder = filePath.substring(0, lastSlashIndex);
        String fileName = filePath.substring(lastSlashIndex + 1);
        // 调用 service 层处理
        ftpService.streamFile(remoteFolder, fileName, isPreview, response);
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
        private String data;

        public Response(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Response(int code, String msg, String data) {
            this.code = code;
            this.msg = msg;
            this.data = data;
        }

        public static Response success(String msg, String data) {
            return new Response(200, msg, data);
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

        public String getData() {
            return data;
        }
    }

    /**
     * 公共文件上传处理方法
     * 完成参数校验、文件名处理、临时文件保存、（可选）PDF 转换、FTP 上传、数据库记录保存、临时文件清理
     *
     * @param file               上传的文件（数据流）
     * @param documentTypeID     文档类型ID（必填）
     * @param matchID            匹配ID（必填）
     * @param planTrackingNumber 计划跟踪号（选填）
     * @param convertToPdf       是否需要将文件转换为 PDF（true 表示转换，false 表示直接上传原文件）
     * @return Response，包含文件ID或错误信息
     */
    private Response processFileUpload(MultipartFile file,
                                       Integer documentTypeID,
                                       String matchID,
                                       String planTrackingNumber,
                                       boolean convertToPdf,
                                       String createdBy) {
        // 参数校验
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

        // 生成时间戳（用于版本号及文件名唯一性）
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        // 处理原始文件名
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            originalFileName = "unknown";
        }
        // 去除原始扩展名（后续根据是否转换决定扩展名）
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex != -1) {
            originalFileName = originalFileName.substring(0, dotIndex);
        }

        // 根据是否转换决定新文件名
        String newFileName;
        if (convertToPdf) {
            newFileName = originalFileName + "_" + timestamp + ".pdf";
        } else {
            // 若不转换，保留原扩展名（通过 MultipartFile 重新获取原始扩展名）
            String ext = "";
            String ori = file.getOriginalFilename();
            if (ori != null && ori.lastIndexOf('.') != -1) {
                ext = ori.substring(ori.lastIndexOf('.'));
            }
            newFileName = originalFileName + "_" + timestamp + ext;
        }

        // 获取临时存储目录（通过 ftpService 提供）
        String tempDir = ftpService.getTempDir();
        File tempDirectory = new File(tempDir);
        if (!tempDirectory.exists() && !tempDirectory.mkdirs()) {
            log.warn("无法创建临时目录: {}", tempDir);
            return Response.error("无法创建临时目录: " + tempDir);
        }

        // 定义本地文件路径：如果转换为 PDF，需要先保存原始文件再转换，否则直接保存目标文件
        String localOriginalPath = tempDir + originalFileName + "_" + timestamp;
        String localTargetPath = tempDir + newFileName;

        try {
            if (convertToPdf) {
                // 保存上传的原始文件到本地，用于转换
                File localOriginalFile = new File(localOriginalPath);
                file.transferTo(localOriginalFile);
                log.info("原始文件保存到本地临时路径: {}", localOriginalPath);

                // 调用转换方法，将原始文件转换为 PDF
                boolean conversionResult = convertToPdf(localOriginalFile, new File(localTargetPath));
                if (!conversionResult) {
                    log.error("文件转换为 PDF 失败");
                    return Response.error("文件转换为 PDF 失败");
                }
                log.info("文件成功转换为 PDF: {}", localTargetPath);
            } else {
                // 不转换：直接将上传的文件保存到目标路径
                file.transferTo(new File(localTargetPath));
                log.info("文件保存到本地临时路径: {}", localTargetPath);
            }

            // 查找或创建对应的文件夹（例如以文档类型名称命名）
            CYFolder folder = folderService.findOrCreateFolder(documentTypeName, "system");
            String remoteFolderPath = ftpService.getRemoteFolderPath(folder.getPhysicalPath());

            // 上传目标文件到 FTP 服务器
            boolean uploadResult = ftpService.uploadFile(localTargetPath, remoteFolderPath, newFileName);
            if (!uploadResult) {
                log.error("文件上传到 FTP 失败: {}", newFileName);
                return Response.error("文件上传到 FTP 失败");
            }
            log.info("文件成功上传到 FTP: {}", newFileName);

            // 构建文件 URL
            String fileURL = ftpService.getFtpUrl(remoteFolderPath, newFileName);
            if (fileURL == null) {
                log.warn("构建文件 URL 失败");
                return Response.error("构建文件 URL 失败");
            }

            // 构建文件记录对象
            CYFile cyFile = new CYFile();
            // 记录文件名按转换后或原始文件名保存
            cyFile.setFileName(newFileName);
            cyFile.setFolderID(folder.getFolderID());
            cyFile.setDocumentTypeName(documentTypeName);
            cyFile.setDocumentTypeID(documentTypeID);
            cyFile.setMatchID(matchID);
            cyFile.setVersionNumber(timestamp);
            cyFile.setCreatedBy(createdBy);
            cyFile.setFileURL(fileURL);
            cyFile.setPlanTrackingNumber(planTrackingNumber);

            // 调用服务层生成文件ID，并设置到记录中
            String fileID = fileService.generateFileID(documentTypeID);
            cyFile.setFileID(fileID);

            // 保存文件记录到数据库
            int insertResult = fileService.saveFileRecord(cyFile);
            if (insertResult <= 0) {
                log.warn("数据库插入文件记录失败");
                return Response.error("数据库插入文件记录失败");
            }
            log.info("文件记录已插入数据库: 文件名={}, 文件夹编码={}, 文件ID={}",
                    cyFile.getFileName(), folder.getFolderCode(), cyFile.getFileID());

            // 清理本地临时文件
            // 如果转换，则删除原始文件和转换后的 PDF；否则只删除保存的目标文件
            if (convertToPdf) {
                File origFile = new File(localOriginalPath);
                File pdfFile = new File(localTargetPath);
                boolean deletedOrig = origFile.delete();
                boolean deletedPdf = pdfFile.delete();
                log.info("临时文件删除结果: 原始文件删除{}，PDF文件删除{}", deletedOrig ? "成功" : "失败", deletedPdf ? "成功" : "失败");
            } else {
                boolean deleted = new File(localTargetPath).delete();
                log.info("临时文件删除{}", deleted ? "成功" : "失败");
            }

            return Response.success("文件上传成功并已存储到数据库", cyFile.getFileID());
        } catch (Exception e) {
            log.error("文件上传或处理失败: {}", e.getMessage(), e);
            return Response.error("文件上传或处理失败: " + e.getMessage());
        }
    }

    /**
     * 将原始文件转换为 PDF 文件
     * <p>
     * 此方法为占位示例，实际项目中请引入 iText、PDFBox 等工具库实现格式转换
     *
     * @param originalFile 原始文件（任意格式）
     * @param pdfFile      目标 PDF 文件
     * @return true 表示转换成功；false 表示转换失败
     */
    private boolean convertToPdf(File originalFile, File pdfFile) {
        // 假设原始文件为纯文本文件，下面将其内容写入 PDF
        try {
            // 读取原始文本内容
            String content = FileUtils.readFileToString(originalFile, StandardCharsets.UTF_8);

            // 创建 PDF 文档对象
            try (PDDocument document = new PDDocument()) {
                // 创建新页面（使用标准A4纸尺寸）
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                // 创建内容流，向页面中写入内容
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    // 设置起始位置与字体
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    // 左上角为 (0,页面高度)，这里设置边距
                    float margin = 50;
                    float yStart = page.getMediaBox().getHeight() - margin;
                    contentStream.newLineAtOffset(margin, yStart);

                    // 每行最大字符数，可根据实际页面宽度进行调整
                    int maxCharsPerLine = 80;
                    // 将内容按换行符分割（同时也处理长行换行）
                    for (String line : content.split("\n")) {
                        // 对长行进行分割
                        while (line.length() > maxCharsPerLine) {
                            String subLine = line.substring(0, maxCharsPerLine);
                            contentStream.showText(subLine);
                            contentStream.newLineAtOffset(0, -15); // 换行，下移15个单位
                            line = line.substring(maxCharsPerLine);
                        }
                        contentStream.showText(line);
                        contentStream.newLineAtOffset(0, -15); // 每写完一行，换行
                    }
                    contentStream.endText();
                }
                // 保存 PDF 文件到目标路径
                document.save(pdfFile);
            }
            return true;
        } catch (Exception e) {
            // 记录转换错误信息
            log.error("convertToPdf 出现异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Anonymous
    @GetMapping("/getFileIDs")
    public AjaxResult getFileIDs(@RequestParam(value = "matchID", required = false) String matchID,
                                 @RequestParam(value = "documentTypeID", required = false) Integer documentTypeID) {
        try {
            // 参数校验，至少提供一个参数
            if (matchID == null && documentTypeID == null) {
                return AjaxResult.error("至少提供 matchID 或 documentTypeID 参数");
            }

            List<String> fileIDs;

            // 根据不同的参数组合来查询文件ID
            if (matchID != null && documentTypeID != null) {
                // 同时提供 matchID 和 documentTypeID
                fileIDs = fileService.getFileIDsByMatchIDAndDocumentTypeID(matchID, documentTypeID);
            } else if (matchID != null) {
                // 只提供 matchID
                fileIDs = fileService.getFileIDsByMatchID(matchID);
            } else {
                // 只提供 documentTypeID
                fileIDs = fileService.getFileIDsByDocumentTypeID(documentTypeID);
            }

            return AjaxResult.success(fileIDs);
        } catch (Exception e) {
            return AjaxResult.error("查询文件ID失败", e.getMessage());
        }
    }

}
