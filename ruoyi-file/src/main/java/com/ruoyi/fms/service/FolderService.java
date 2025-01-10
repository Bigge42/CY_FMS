package com.ruoyi.fms.service;

import com.ruoyi.fms.domain.CYFolder;
import com.ruoyi.fms.mapper.CYFolderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FolderService {

    @Autowired
    private CYFolderMapper folderMapper;

    /**
     * 查找或创建文件夹
     *
     * @param documentTypeName 文档类型名称
     * @param createdBy        创建者
     * @return CYFolder 对象
     */
    public CYFolder findOrCreateFolder(String documentTypeName, String createdBy) {
        // 查找文件夹
        CYFolder folder = folderMapper.findByFolderName(documentTypeName);
        if (folder != null) {
            return folder;
        }

        // 创建新文件夹
        CYFolder newFolder = new CYFolder();
        newFolder.setFolderName(documentTypeName);
        newFolder.setFolderCode(generateFolderCode());
        newFolder.setPhysicalPath(documentTypeName); // 物理路径可以根据需要调整
        newFolder.setCreatedBy(createdBy);

        folderMapper.insertFolder(newFolder);
        return newFolder;
    }

    /**
     * 生成文件夹代码（可以根据需求调整）
     *
     * @return 文件夹代码
     */
    private String generateFolderCode() {
        return "FOLDER-" + System.currentTimeMillis();
    }

    /**
     * 根据 FolderID 查找文件夹
     *
     * @param folderID 文件夹ID
     * @return CYFolder 对象
     */
    public CYFolder findFolderById(Integer folderID) {
        return folderMapper.findById(folderID);
    }
}
