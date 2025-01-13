package com.ruoyi.fms.service;

import com.ruoyi.fms.domain.CYFile;
import com.ruoyi.fms.mapper.CYFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public int markFileAsDeleted(Integer fileID) {
        return fileMapper.markAsDeleted(fileID);
    }
}
