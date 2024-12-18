package com.ruoyi.fms.mapper;

import java.util.List;
import com.ruoyi.fms.domain.CyFolder;

/**
 * 文件夹Mapper接口
 * 
 * @author ruoyi
 * @date 2024-12-11
 */
public interface CyFolderMapper 
{
    /**
     * 查询文件夹
     * 
     * @param folderId 文件夹主键
     * @return 文件夹
     */
    public CyFolder selectCyFolderByFolderId(Long folderId);

    /**
     * 查询文件夹列表
     * 
     * @param cyFolder 文件夹
     * @return 文件夹集合
     */
    public List<CyFolder> selectCyFolderList(CyFolder cyFolder);

    /**
     * 新增文件夹
     * 
     * @param cyFolder 文件夹
     * @return 结果
     */
    public int insertCyFolder(CyFolder cyFolder);

    /**
     * 修改文件夹
     * 
     * @param cyFolder 文件夹
     * @return 结果
     */
    public int updateCyFolder(CyFolder cyFolder);

    /**
     * 删除文件夹
     * 
     * @param folderId 文件夹主键
     * @return 结果
     */
    public int deleteCyFolderByFolderId(Long folderId);

    /**
     * 批量删除文件夹
     * 
     * @param folderIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCyFolderByFolderIds(Long[] folderIds);
}
