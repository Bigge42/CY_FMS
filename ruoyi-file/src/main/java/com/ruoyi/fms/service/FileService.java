package com.ruoyi.fms.service;

import com.ruoyi.fms.domain.CYFile;
import com.ruoyi.fms.mapper.CYFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class FileService {

    @Autowired
    private CYFileMapper fileMapper;

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

    /**
     * 根据 DocumentTypeID 获取对应的文档类型缩写
     *
     * @param documentTypeID 文档类型ID
     * @return 文档类型缩写，如果不存在则返回 "UNK"
     */
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


}
