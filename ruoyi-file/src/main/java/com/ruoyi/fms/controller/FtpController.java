package com.ruoyi.fms.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.fms.domain.CYFile;
import com.ruoyi.fms.domain.CYFolder;
import com.ruoyi.fms.service.FileService;
import com.ruoyi.fms.service.FolderService;
import com.ruoyi.fms.service.FtpService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import org.apache.commons.io.IOUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
            "Spray Welding Report",                // 喷焊报告
            "Marking",                             // 唛头
            "Pneumatic Circuit Diagram",           // 气路图
            "Exterior Dimension Drawing",          // 外形尺寸图
            "Calculation Report",               // 计算书
            "Supplier Raw Material Attachment",    // 供应商原材料附件
            "OA",
            "MOM File"
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
            case 13:
                return "Marking";                            // 唛头
            case 15:
                return "Pneumatic Circuit Diagram"; //  气路图
            case 16:
                return "Exterior Dimension Drawing"; //  外形尺寸图
            case 17:
                return "Calculation Report"; //  计算书
            case 18:
                return "Supplier Raw Material Attachment";// 供应商原材料附件
            case 19:
                return "OA"; //OA
            case 20:
                return "MOM File";
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



    /**
     * TCfiles 接口：传入物料编码，返回可下载文件的列表（code + urls）
     * 示例：
     *   GET /ftp/TCfiles?code=ABC.123
     */
    @Anonymous
    @GetMapping("/TCfiles")
    public AjaxResult TCfiles(@RequestParam String code, HttpServletRequest request) throws UnsupportedEncodingException {
        String base = ServletUriComponentsBuilder
                .fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
        FtpService.FileUrlResponse data = ftpService.TCBuildFileUrls(code, base);

        return AjaxResult.success(data);
    }



    /**
     * TCdownload 接口：接受已经 URL 编码过的 path 参数，
     * 解码后交给 Service 层下载
     */
    @Anonymous
    @GetMapping("/TCdownload")
    public void TCdownload(
            @RequestParam("path") String pathEncoded,
            HttpServletResponse response) throws UnsupportedEncodingException {

        // 1. 把 %XX 恢复成原始路径
        String fullPath = URLDecoder.decode(pathEncoded, StandardCharsets.UTF_8.name());

        // 2. 调用 Service 层（重载方法）进行目录分离 + 下载逻辑
        ftpService.TCDownload(fullPath, response);
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

        // URL 编码文件名部分，避免中文字符导致问题
        try {
            // 只对文件名进行编码，保留文件夹路径不编码
            String encodedFileName = URLEncoder.encode(newFileName, StandardCharsets.UTF_8.toString());
            log.info("文件名经过 URL 编码处理: {}", encodedFileName);

            // 获取临时存储目录（通过 ftpService 提供）
            String tempDir = ftpService.getTempDir();
            File tempDirectory = new File(tempDir);
            if (!tempDirectory.exists() && !tempDirectory.mkdirs()) {
                log.warn("无法创建临时目录: {}", tempDir);
                return Response.error("无法创建临时目录: " + tempDir);
            }

            // 定义本地文件路径：如果转换为 PDF，需要先保存原始文件再转换，否则直接保存目标文件
            String localOriginalPath = tempDir + originalFileName + "_" + timestamp;
            String localTargetPath = tempDir + encodedFileName;

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
                boolean uploadResult = ftpService.uploadFile(localTargetPath, remoteFolderPath, encodedFileName);
                if (!uploadResult) {
                    log.error("文件上传到 FTP 失败: {}", encodedFileName);
                    return Response.error("文件上传到 FTP 失败");
                }
                log.info("文件成功上传到 FTP: {}", encodedFileName);

                // 构建文件 URL，保留路径部分不编码
                String fileURL = ftpService.getFtpUrl(remoteFolderPath, encodedFileName);
                if (fileURL == null) {
                    log.warn("构建文件 URL 失败");
                    return Response.error("构建文件 URL 失败");
                }

                // 构建文件记录对象
                CYFile cyFile = new CYFile();
                cyFile.setFileName(encodedFileName);
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

                return Response.success("文件上传成功并已存  储到数据库", cyFile.getFileID());
            } catch (Exception e) {
                log.error("文件上传或处理失败: {}", e.getMessage(), e);
                return Response.error("文件上传或处理失败: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("文件名编码失败: {}", e.getMessage());
            return Response.error("文件名编码失败");
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

    /**
     * 批量 ZIP 下载
     * GET /fms/ftp/downloadZip?fileIds=ID1&fileIds=ID2...
     */
    @Anonymous
    @GetMapping("/downloadZip")
    public void downloadZip(@RequestParam("fileIds") List<String> fileIds,
                            HttpServletResponse response) throws IOException {
        // 1) 设置响应头
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"files.zip\"");

        // 2) 交给 Service 去拉流、打 ZIP
        ftpService.writeFilesToZip(fileIds, response.getOutputStream());
    }



    /**
     * 批量文件ID接口
     *
     * @param matchID         匹配ID（必填）
     * @param documentTypeID 文档类型ID（必填）
     * @param planTrackingNumber 可选的计划跟踪编号（如果提供，必须提供 matchID）
     * @return fileIDs
     */
    @Anonymous
    @GetMapping("/getFileIDs")
    public AjaxResult getFileIDs(@RequestParam(value = "matchID", required = false) String matchID,
                                 @RequestParam(value = "documentTypeID", required = false) Integer documentTypeID,
                                 @RequestParam(value = "PlanTrackingNumber", required = false) String planTrackingNumber) {
        try {
            // 如果传入 PlanTrackingNumber，则必须同时提供 matchID
            if (planTrackingNumber != null && matchID == null) {
                return AjaxResult.error("当提供 PlanTrackingNumber 参数时，必须同时提供 matchID 参数");
            }

            // 至少需要提供一个参数
            if (matchID == null && documentTypeID == null && planTrackingNumber == null) {
                return AjaxResult.error("至少提供 matchID、documentTypeID 或 PlanTrackingNumber 参数");
            }

            List<String> fileIDs;

            // 根据参数组合调用对应的查询方法
            if (matchID != null && documentTypeID != null && planTrackingNumber != null) {
                // 同时提供 matchID、documentTypeID 和 PlanTrackingNumber
                fileIDs = fileService.getFileIDsByMatchIDAndDocumentTypeIDAndPlanTrackingNumber(matchID, documentTypeID, planTrackingNumber);
            } else if (matchID != null && documentTypeID != null) {
                // 同时提供 matchID 和 documentTypeID
                fileIDs = fileService.getFileIDsByMatchIDAndDocumentTypeID(matchID, documentTypeID);
            } else if (matchID != null && planTrackingNumber != null) {
                // 同时提供 matchID 和 PlanTrackingNumber
                fileIDs = fileService.getFileIDsByMatchIDAndPlanTrackingNumber(matchID, planTrackingNumber);
            } else if (documentTypeID != null && planTrackingNumber != null) {
                // 此组合无效：PlanTrackingNumber 必须与 matchID 一起使用
                return AjaxResult.error("PlanTrackingNumber 参数必须与 matchID 参数一起提供");
            } else if (matchID != null) {
                // 仅提供 matchID
                fileIDs = fileService.getFileIDsByMatchID(matchID);
            } else if (documentTypeID != null) {
                // 仅提供 documentTypeID
                fileIDs = fileService.getFileIDsByDocumentTypeID(documentTypeID);
            } else {
                // 不会执行到这里，因为所有情况都已处理
                return AjaxResult.error("参数不合法");
            }

            return AjaxResult.success(fileIDs);
        } catch (Exception e) {
            return AjaxResult.error("查询文件ID失败", e.getMessage());
        }
    }

    /**
     * 批量查询接口
     *
     * @param matchID         匹配ID（必填）
     * @param documentTypeIDs 文档类型ID集合（必填，可以传入多个，例如：documentTypeIDs=1,2,3）
     * @param planTrackingNumber 可选的计划跟踪编号（如果提供，必须提供 matchID）
     * @return 返回结果示例: [{ "documentTypeID": 2, "fileID": "12212" }, { "documentTypeID": 3, "fileID": "12213" }]
     */
    @Anonymous
    @GetMapping("/batchGetFileIDs")
    public AjaxResult batchGetFileIDs(@RequestParam("matchID") String matchID,
                                      @RequestParam("documentTypeIDs") List<Integer> documentTypeIDs,
                                      @RequestParam(value = "PlanTrackingNumber", required = false) String planTrackingNumber) {
        try {
            // 如果传入 PlanTrackingNumber，则必须同时提供 matchID
            if (planTrackingNumber != null && (matchID == null || matchID.trim().isEmpty())) {
                return AjaxResult.error("当提供 PlanTrackingNumber 参数时，必须同时提供 matchID 参数");
            }

            // 参数校验：至少提供 matchID 和非空的 documentTypeIDs 集合
            if (matchID == null || matchID.trim().isEmpty() || documentTypeIDs == null || documentTypeIDs.isEmpty()) {
                return AjaxResult.error("必须提供 matchID 和至少一个 documentTypeID");
            }

            // 查询文件ID集合
            List<Map<String, Object>> result;
            if (planTrackingNumber != null) {
                // 同时提供 matchID 和 PlanTrackingNumber
                result = fileService.getFileIDsByMatchIDAndDocumentTypeIDsAndPlanTrackingNumber(matchID, documentTypeIDs, planTrackingNumber);
            } else {
                // 仅提供 matchID 和 documentTypeIDs
                result = fileService.getFileIDsByMatchIDAndDocumentTypeIDs(matchID, documentTypeIDs);
            }

            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("批量查询文件ID失败", e.getMessage());
        }
    }

    @Anonymous
    @PostMapping(
            value = "/batchProcessUpload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public AjaxResult batchProcessUpload(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam("matchID") String matchID,
            @RequestParam(value = "PlanTrackingNumber", required = false) String planTrackingNumber,
            @RequestParam(value = "convertToPdf", defaultValue = "false") boolean convertToPdf,
            HttpServletRequest request) throws IOException {

        final long reqStartNs = System.nanoTime();
        final String reqId = UUID.randomUUID().toString().substring(0, 8);
        final String clientIp = firstNonEmpty(request.getHeader("X-Forwarded-For"), request.getRemoteAddr());
        long totalBytes = 0L;
        int successCount = 0;

        // 参数快速校验（失败也要带耗时）
        if (files == null || files.isEmpty()) {
            long elapsedMs = nsToMs(System.nanoTime() - reqStartNs);
            log.warn("upload.fail reqId={} client={} matchID={} reason='empty files' elapsedMs={}",
                    reqId, clientIp, matchID, elapsedMs);
            return AjaxResult.error("请至少上传一个文件");
        }
        if (matchID == null || matchID.trim().isEmpty()) {
            long elapsedMs = nsToMs(System.nanoTime() - reqStartNs);
            log.warn("upload.fail reqId={} client={} reason='empty matchID' elapsedMs={}",
                    reqId, clientIp, elapsedMs);
            return AjaxResult.error("matchID 不能为空");
        }

        final String tempDir = ftpService.getTempDir();
        if (tempDir == null || tempDir.trim().isEmpty()) {
            long elapsedMs = nsToMs(System.nanoTime() - reqStartNs);
            log.warn("upload.fail reqId={} client={} matchID={} reason='tempDir not configured' elapsedMs={}",
                    reqId, clientIp, matchID, elapsedMs);
            return AjaxResult.error("临时目录未配置");
        }
        File tmpRoot = new File(tempDir);
        if (!tmpRoot.exists() && !tmpRoot.mkdirs()) {
            long elapsedMs = nsToMs(System.nanoTime() - reqStartNs);
            log.warn("upload.fail reqId={} client={} matchID={} reason='mkdirs failed' dir='{}' elapsedMs={}",
                    reqId, clientIp, matchID, tempDir, elapsedMs);
            return AjaxResult.error("无法创建临时目录: " + tempDir);
        }

        List<String> fileIds = new ArrayList<>();
        // 每个文件的阶段日志
        for (MultipartFile mf : files) {
            if (mf.isEmpty()) {
                long elapsedMs = nsToMs(System.nanoTime() - reqStartNs);
                log.warn("upload.fail reqId={} client={} matchID={} reason='one empty file' elapsedMs={}",
                        reqId, clientIp, matchID, elapsedMs);
                return AjaxResult.error("存在空文件");
            }

            final long fileStartNs = System.nanoTime();
            final long declaredSize = mf.getSize();         // 由客户端声明的大小（不一定等于最终写入）
            totalBytes += Math.max(0, declaredSize);

            // 1) 文件名处理
            String rawName = mf.getOriginalFilename();
            String name0 = (rawName == null || rawName.isEmpty()) ? "unnamed" : rawName;
            String decodedName = smartDecodeFilename(name0);
            String safeOriginalName = sanitizeWinFileName(decodedName);
            String ext = getExt(safeOriginalName);

            // 2) 落盘
            long saveStart = System.nanoTime();
            Path localPath = Files.createTempFile(tmpRoot.toPath(), "upload_", ext.isEmpty() ? "" : ("." + ext));
            long bytesWritten;
            try (InputStream in = mf.getInputStream()) {
                bytesWritten = Files.copy(in, localPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException io) {
                long elapsedMs = nsToMs(System.nanoTime() - reqStartNs);
                long saveMs = nsToMs(System.nanoTime() - saveStart);
                log.warn("upload.fail reqId={} client={} matchID={} stage=TEMP_SAVE file='{}' size={}B saveMs={} err='{}' totalElapsedMs={}",
                        reqId, clientIp, matchID, safeOriginalName, declaredSize, saveMs, io.getMessage(), elapsedMs);
                return AjaxResult.error("写入临时文件失败: " + io.getMessage());
            }
            long saveMs = nsToMs(System.nanoTime() - saveStart);

            // 3) FTP 上传
            long ftpStart = System.nanoTime();
            String subFolder = "OA/" + matchID;
            boolean ok = ftpService.uploadThenRenameByListing(localPath.toString(), subFolder, safeOriginalName);
            long ftpMs = nsToMs(System.nanoTime() - ftpStart);
            try { Files.deleteIfExists(localPath); } catch (IOException ignore) {}

            if (!ok) {
                long elapsedMs = nsToMs(System.nanoTime() - reqStartNs);
                log.warn("upload.fail reqId={} client={} matchID={} stage=FTP_UPLOAD file='{}' size={}B saveMs={} ftpMs={} totalElapsedMs={}",
                        reqId, clientIp, matchID, safeOriginalName, bytesWritten, saveMs, ftpMs, elapsedMs);
                return AjaxResult.error("上传并重命名失败: " + safeOriginalName);
            }

            // 4) 成功一个文件
            long oneElapsedMs = nsToMs(System.nanoTime() - fileStartNs);
            String fileID = fileService.generateFileID(19);
            fileIds.add(fileID);
            successCount++;

            log.info("upload.file.ok reqId={} client={} matchID={} file='{}' size={}B saveMs={} ftpMs={} fileElapsedMs={}",
                    reqId, clientIp, matchID, safeOriginalName, bytesWritten, saveMs, ftpMs, oneElapsedMs);
        }

        // 总结日志（全部成功）
        long totalMs = nsToMs(System.nanoTime() - reqStartNs);
        double mb = totalBytes / (1024.0 * 1024.0);
        double rate = (totalMs > 0) ? (mb / (totalMs / 1000.0)) : 0.0; // MB/s

        log.info("upload.done reqId={} client={} matchID={} files={} success={} totalBytes={}B ({:.2f}MB) elapsedMs={} rate={:.2f}MBps",
                reqId, clientIp, matchID, files.size(), successCount, totalBytes, mb, totalMs, rate);

        // 可选：把统计信息返回给前端（不需要可删除）
        Map<String, Object> stats = new HashMap<>();
        stats.put("reqId", reqId);
        stats.put("files", files.size());
        stats.put("success", successCount);
        stats.put("totalBytes", totalBytes);
        stats.put("elapsedMs", totalMs);
        stats.put("rateMBps", round2(rate));

        AjaxResult okRes = AjaxResult.success(fileIds);
        okRes.put("stats", stats);
        return okRes;
    }

    // ---------- 辅助方法 ----------
    private static long nsToMs(long ns) { return Duration.ofNanos(ns).toMillis(); }
    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    // 替代 String#isBlank —— 兼容 JDK8
    private static boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // 取第一个非空非空白字符串（用于获取客户端 IP 等场景）
    private static String firstNonEmpty(String... vals) {
        if (vals == null) return null;
        for (String v : vals) {
            if (!isNullOrBlank(v)) return v;
        }
        return null;
    }

    /** 仅去掉 Windows 非法字符，并处理保留名/结尾空格点/过长等 */
    private static String sanitizeWinFileName(String name) {
        String trimmed = name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        // 保留名处理
        String base = trimmed.replaceAll("[. ]+$", ""); // 去掉结尾的点/空格
        if (base.isEmpty()) base = "unnamed";
        String upper = base.toUpperCase();
        Set<String> reserved = new HashSet<>(Arrays.asList(
                "CON","PRN","AUX","NUL","COM1","COM2","COM3","COM4","COM5","COM6","COM7","COM8","COM9",
                "LPT1","LPT2","LPT3","LPT4","LPT5","LPT6","LPT7","LPT8","LPT9"
        ));
        if (reserved.contains(upper)) base = "_" + base;
        return base.length() > 180 ? base.substring(0, 180) : base;
    }

    private static String getExt(String name) {
        int i = name.lastIndexOf('.');
        return (i > 0 && i < name.length() - 1) ? name.substring(i + 1) : "";
    }

    private static final java.util.regex.Pattern HEX_ENC   = java.util.regex.Pattern.compile("%[0-9A-Fa-f]{2}");
    private static final java.util.regex.Pattern RFC5987   = java.util.regex.Pattern.compile("(?i)^([A-Z0-9\\-]+)''(.+)$");
    // 例如 "UTF-8''%E6%8A%A5%E5%91%8A.pdf"

    private static String smartDecodeFilename(String s) {
        if (s == null || s.isEmpty()) return "unnamed";

        // ① 先兼容 filename*=charset''%XX... 这种 RFC 5987 形式（偶尔会直接出现在 originalFilename）
        java.util.regex.Matcher m = RFC5987.matcher(s);
        if (m.matches()) {
            String cs  = m.group(1);     // charset
            String enc = m.group(2);     // %XX...
            try {
                return java.net.URLDecoder.decode(enc, java.nio.charset.Charset.forName(cs).name());
            } catch (Exception ignore) { /* 回退到 UTF-8 逻辑 */ }
        }

        // ② 常规：仅当出现 %XX 才尝试解码，避免“好端端的中文名被二次解坏”
        if (HEX_ENC.matcher(s).find()) {
            try {
                // 保护 '+'，避免被 URLDecoder 当成空格
                String t = s.replace("+", "%2B");
                String once = java.net.URLDecoder.decode(t, java.nio.charset.StandardCharsets.UTF_8.name());
                // 若还是有 %XX，最多再解一次（防双重编码）
                if (HEX_ENC.matcher(once).find()) {
                    String twice = java.net.URLDecoder.decode(once.replace("+", "%2B"), java.nio.charset.StandardCharsets.UTF_8.name());
                    return twice;
                }
                return once;
            } catch (Exception ignore) {
                // 忽略异常，直接返回原名
            }
        }

        // ③ 看起来没有 URL 编码，就原样返回
        return s;
    }
}
