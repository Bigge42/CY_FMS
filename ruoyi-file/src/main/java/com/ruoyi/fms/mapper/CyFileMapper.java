package com.ruoyi.fms.mapper;

import java.util.List;
import com.ruoyi.fms.domain.CyFile;

/**
 * 文件信息Mapper接口
 * 
 * @author ruoyi
 * @date 2024-12-11
 */
public interface CyFileMapper 
{
    /**
     * 查询文件信息
     * 
     * @param fileId 文件信息主键
     * @return 文件信息
     */
    public CyFile selectCyFileByFileId(Long fileId);

    /**
     * 查询文件信息列表
     * 
     * @param cyFile 文件信息
     * @return 文件信息集合
     */
    public List<CyFile> selectCyFileList(CyFile cyFile);

    /**
     * 新增文件信息
     * 
     * @param cyFile 文件信息
     * @return 结果
     */
    public int insertCyFile(CyFile cyFile);

    /**
     * 修改文件信息
     * 
     * @param cyFile 文件信息
     * @return 结果
     */
    public int updateCyFile(CyFile cyFile);

    /**
     * 删除文件信息
     * 
     * @param fileId 文件信息主键
     * @return 结果
     */
    public int deleteCyFileByFileId(Long fileId);

    /**
     * 批量删除文件信息
     * 
     * @param fileIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCyFileByFileIds(Long[] fileIds);
}
