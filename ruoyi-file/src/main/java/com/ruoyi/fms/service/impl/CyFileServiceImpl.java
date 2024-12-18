package com.ruoyi.fms.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.fms.mapper.CyFileMapper;
import com.ruoyi.fms.domain.CyFile;
import com.ruoyi.fms.service.ICyFileService;

/**
 * 文件信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-12-11
 */
@Service
public class CyFileServiceImpl implements ICyFileService 
{
    @Autowired
    private CyFileMapper cyFileMapper;

    /**
     * 查询文件信息
     * 
     * @param fileId 文件信息主键
     * @return 文件信息
     */
    @Override
    public CyFile selectCyFileByFileId(Long fileId)
    {
        return cyFileMapper.selectCyFileByFileId(fileId);
    }

    /**
     * 查询文件信息列表
     * 
     * @param cyFile 文件信息
     * @return 文件信息
     */
    @Override
    public List<CyFile> selectCyFileList(CyFile cyFile)
    {
        return cyFileMapper.selectCyFileList(cyFile);
    }

    /**
     * 新增文件信息
     * 
     * @param cyFile 文件信息
     * @return 结果
     */
    @Override
    public int insertCyFile(CyFile cyFile)
    {
        cyFile.setCreateTime(DateUtils.getNowDate());
        return cyFileMapper.insertCyFile(cyFile);
    }

    /**
     * 修改文件信息
     * 
     * @param cyFile 文件信息
     * @return 结果
     */
    @Override
    public int updateCyFile(CyFile cyFile)
    {
        cyFile.setUpdateTime(DateUtils.getNowDate());
        return cyFileMapper.updateCyFile(cyFile);
    }

    /**
     * 批量删除文件信息
     * 
     * @param fileIds 需要删除的文件信息主键
     * @return 结果
     */
    @Override
    public int deleteCyFileByFileIds(Long[] fileIds)
    {
        return cyFileMapper.deleteCyFileByFileIds(fileIds);
    }

    /**
     * 删除文件信息信息
     * 
     * @param fileId 文件信息主键
     * @return 结果
     */
    @Override
    public int deleteCyFileByFileId(Long fileId)
    {
        return cyFileMapper.deleteCyFileByFileId(fileId);
    }
}
