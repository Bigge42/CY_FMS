package com.ruoyi.fms.service;

import com.ruoyi.fms.domain.CYFile;
import com.ruoyi.fms.domain.CYFolder;
import com.ruoyi.fms.mapper.FileMapper;
import com.ruoyi.fms.mapper.FolderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class FileService {

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private FolderMapper folderMapper;

    @Transactional
    public void saveFileRecord(String fileName, String localFilePath, String folderCode) {
        // 检查文件夹是否存在
        CYFolder folder = folderMapper.findByFolderCode(folderCode);
        if (folder == null) {
            // 如果文件夹不存在，动态创建
            folder = new CYFolder();
            folder.setFolderCode(folderCode);
            folder.setFolderName("默认文件夹");
            folder.setCreatedBy("system");
            folderMapper.insertFolder(folder);
        }

        // 构建文件记录
        CYFile fileRecord = new CYFile();
        fileRecord.setFileName(fileName);
        fileRecord.setFolderID(folder.getFolderID());
        fileRecord.setDocumentTypeName("未分类"); // 默认文档类型
        fileRecord.setVersionNumber("1.0");
        fileRecord.setCreatedBy("system");
        fileRecord.setFileURL(localFilePath);

        // 插入文件记录到数据库
        fileMapper.insertFile(fileRecord);
    }
}


