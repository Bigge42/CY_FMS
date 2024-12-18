package com.ruoyi.fms.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.fms.mapper.CyFolderMapper;
import com.ruoyi.fms.domain.CyFolder;
import com.ruoyi.fms.service.ICyFolderService;

/**
 * 文件夹Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-12-11
 */
@Service
public class CyFolderServiceImpl implements ICyFolderService 
{
    @Autowired
    private CyFolderMapper cyFolderMapper;

    /**
     * 查询文件夹
     * 
     * @param folderId 文件夹主键
     * @return 文件夹
     */
    @Override
    public CyFolder selectCyFolderByFolderId(Long folderId)
    {
        return cyFolderMapper.selectCyFolderByFolderId(folderId);
    }

    /**
     * 查询文件夹列表
     * 
     * @param cyFolder 文件夹
     * @return 文件夹
     */
    @Override
    public List<CyFolder> selectCyFolderList(CyFolder cyFolder)
    {
        return cyFolderMapper.selectCyFolderList(cyFolder);
    }

    /**
     * 新增文件夹
     * 
     * @param cyFolder 文件夹
     * @return 结果
     */
    @Override
    public int insertCyFolder(CyFolder cyFolder)
    {
        cyFolder.setCreateTime(DateUtils.getNowDate());
        return cyFolderMapper.insertCyFolder(cyFolder);
    }

    /**
     * 修改文件夹
     * 
     * @param cyFolder 文件夹
     * @return 结果
     */
    @Override
    public int updateCyFolder(CyFolder cyFolder)
    {
        cyFolder.setUpdateTime(DateUtils.getNowDate());
        return cyFolderMapper.updateCyFolder(cyFolder);
    }

    /**
     * 批量删除文件夹
     * 
     * @param folderIds 需要删除的文件夹主键
     * @return 结果
     */
    @Override
    public int deleteCyFolderByFolderIds(Long[] folderIds)
    {
        return cyFolderMapper.deleteCyFolderByFolderIds(folderIds);
    }

    /**
     * 删除文件夹信息
     * 
     * @param folderId 文件夹主键
     * @return 结果
     */
    @Override
    public int deleteCyFolderByFolderId(Long folderId)
    {
        return cyFolderMapper.deleteCyFolderByFolderId(folderId);
    }
}
