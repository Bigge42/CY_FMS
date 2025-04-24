package com.ruoyi.fms.service;

import com.ruoyi.fms.domain.CYFile;
import com.ruoyi.fms.mapper.CYFileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FileService {

    @Autowired
    private CYFileMapper fileMapper;
    private static final Logger log = LoggerFactory.getLogger(FileService.class);


    /**
     * 保存文件记录到数据库
     *
     * @param file CYFile 对象
     * @return 插入结果
     */
    public int saveFileRecord(CYFile file) {
        return fileMapper.insertFile(file);
    }

    /**
     * 根据文件名和 MatchID 查找文件
     *
     * @param documentTypeName 文件名
     * @param matchID  匹配ID
     * @return CYFile 对象
     */
    public CYFile findFileByNameAndMatchID(String documentTypeName, Integer matchID) {
        return fileMapper.findByFileNameAndMatchID(documentTypeName, matchID);
    }

    /**
     * 标记文件为已删除
     *
     * @param fileID 文件ID
     * @return 更新结果
     */
    public int markFileAsDeleted(String fileID) {
        return fileMapper.markAsDeleted(fileID);
    }

    public String getDocumentTypeAbbreviation(Integer documentTypeID) {
        if (documentTypeID == null) {
            return "UNK"; // Unknown
        }
        switch (documentTypeID) {
            case 1:
                return "MPR"; // Material Physicochemical Report
            case 2:
                return "CER"; // Certificate
            case 3:
                return "MAN"; // Manual
            case 4:
                return "PIR"; // Product Inspection Report
            case 5:
                return "PL";  // Packing List
            case 6:
                return "SRM"; // Supplier Raw Material Report
            case 7:
                return "PP";  // Packing Photo
            case 8:
                return "WR";  // Welding Report
            case 9:
                return "PDR"; // Part Dimension Record Report
            case 10:
                return "HTR"; // Heat Treatment Report
            case 11:
                return "SPR"; // Spraying Report
            case 12:
                return "SWR"; // Spray Welding Report
            case 13:
                return "MM";  // Marking
            case 15:
                return "PCD"; // Pneumatic Circuit Diagram (气路图)
            case 16:
                return "EDD"; // Exterior Dimension Drawing (外形尺寸图)
            case 17:
                return "CAL"; // Calculation Report (计算书)
            default:
                return "UNK"; // Unknown
        }
    }


    /**
     * 根据文档类型ID生成文件ID（缩写 + 时间戳）
     *
     * @param documentTypeID 文档类型ID
     * @return 生成的文件ID
     */
    public String generateFileID(Integer documentTypeID) {
        // 1. 获取文件类型缩写
        String abbreviation = getDocumentTypeAbbreviation(documentTypeID);
        // 2. 生成当前时间戳（精确到毫秒，保证尽量唯一）
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        // 3. 拼接得到最终的 fileID
        return abbreviation + timestamp;
    }


    /**
     * 根据 fileId 查询数据库中对应文件记录，并返回文件的存储路径（例如 fileURL）
     *
     * @param fileId 文件ID
     * @return 文件存储路径，如果未找到则返回 null
     */

    public String getFilePathByFileId(String fileId) {
        CYFile cyFile = fileMapper.selectByFileId(fileId);
        if (cyFile != null) {
            return cyFile.getFileURL();
        }
        return null;
    }

    // 更新文件记录的方法
    public int updateFileRecord(CYFile cyFile) {
        try {
            // 确保文件ID存在
            if (cyFile.getFileID() == null) {
                log.warn("文件ID为空，无法更新文件记录");
                return 0;  // 返回0表示没有更新
            }

            // 将更新日期转换为 MySQL 可接受的格式
            String updatedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            // 调用CYFileMapper来更新文件记录
            int updateCount = fileMapper.updateFile(cyFile.getFileID(),
                    cyFile.getUpdatedBy(),
                    updatedAt,
                    cyFile.getFileURL(),
                    cyFile.getDeleteFlag());

            // 记录日志
            if (updateCount > 0) {
                log.info("文件记录更新成功: FileID={}", cyFile.getFileID());
            } else {
                log.warn("未能更新文件记录: FileID={}", cyFile.getFileID());
            }

            return updateCount;
        } catch (Exception e) {
            log.error("更新文件记录失败: {}", e.getMessage(), e);
            return 0;  // 返回0表示没有更新
        }
    }

    // 根据 fileID 查找文件记录
    public CYFile findFileById(String fileID) {
        try {
            // 调用 CYFileMapper 查找文件
            CYFile cyFile = fileMapper.findFileById(fileID);
            if (cyFile == null) {
                log.warn("未找到对应的文件记录: FileID={}", fileID);
            }
            return cyFile;
        } catch (Exception e) {
            log.error("查找文件记录失败: {}", e.getMessage(), e);
            return null;
        }
    }

    // 根据 matchID 和 documentTypeID 查询文件ID集合
    public List<String> getFileIDsByMatchIDAndDocumentTypeID(String matchID, Integer documentTypeID) {
        return fileMapper.findFileIDsByMatchIDAndDocumentTypeID(matchID, documentTypeID);
    }

    public List<String> getFileIDsByMatchID(String matchID) {
        // 根据 matchID 查询文件ID
        return fileMapper.findFileIDsByMatchID(matchID);
    }

    public List<String> getFileIDsByDocumentTypeID(Integer documentTypeID) {
        // 根据 documentTypeID 查询文件ID
        return fileMapper.findFileIDsByDocumentTypeID(documentTypeID);
    }

    /**
     * 根据 matchID 和多个 documentTypeID 查询文件ID集合
     *
     * @param matchID         匹配ID
     * @param documentTypeIDs 文档类型ID集合
     * @return List<Map<String, Object>> 每个Map包含 documentTypeID 和 fileID
     */
    public List<Map<String, Object>> getFileIDsByMatchIDAndDocumentTypeIDs(String matchID, List<Integer> documentTypeIDs) {
        return fileMapper.findFileIDsByMatchIDAndDocumentTypeIDs(matchID, documentTypeIDs);
    }

    /**
     * 根据 matchID、documentTypeID 和 PlanTrackingNumber 查询文件ID集合
     *
     * @param matchID            匹配ID
     * @param documentTypeID     文档类型ID
     * @param planTrackingNumber 计划跟踪编号
     * @return List<String> 文件ID集合
     */
    public List<String> getFileIDsByMatchIDAndDocumentTypeIDAndPlanTrackingNumber(String matchID, Integer documentTypeID, String planTrackingNumber) {
        return fileMapper.findFileIDsByMatchIDAndDocumentTypeIDAndPlanTrackingNumber(matchID, documentTypeID, planTrackingNumber);
    }

    /**
     * 根据 matchID 和 PlanTrackingNumber 查询文件ID集合
     *
     * @param matchID            匹配ID
     * @param planTrackingNumber 计划跟踪编号
     * @return List<String> 文件ID集合
     */
    public List<String> getFileIDsByMatchIDAndPlanTrackingNumber(String matchID, String planTrackingNumber) {
        return fileMapper.findFileIDsByMatchIDAndPlanTrackingNumber(matchID, planTrackingNumber);
    }
    /**
     * 根据 matchID、documentTypeIDs 和 PlanTrackingNumber 查询文件ID集合
     *
     * @param matchID            匹配ID
     * @param documentTypeIDs     文档类型ID集合
     * @param planTrackingNumber 计划跟踪编号
     * @return List<Map<String, Object>> 每个Map包含 documentTypeID 和 fileID
     */
    public List<Map<String, Object>> getFileIDsByMatchIDAndDocumentTypeIDsAndPlanTrackingNumber(String matchID,
                                                                                                List<Integer> documentTypeIDs,
                                                                                                String planTrackingNumber) {
        return fileMapper.findFileIDsByMatchIDAndDocumentTypeIDsAndPlanTrackingNumber(matchID, documentTypeIDs, planTrackingNumber);
    }

    /**
     * 根据 fileId 列表，批量返回它们的 fileURL
     */
    public List<String> getFilePathsByFileIds(List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return Collections.emptyList();
        }
        return fileMapper.selectFileURLsByIds(fileIds);
    }

}
