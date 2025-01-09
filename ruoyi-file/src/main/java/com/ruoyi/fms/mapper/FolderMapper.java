package com.ruoyi.fms.mapper;

import com.ruoyi.fms.domain.CYFolder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface FolderMapper {
    @Select("SELECT * FROM CY_Folder WHERE FolderCode = #{folderCode}")
    CYFolder findByFolderCode(@Param("folderCode") String folderCode);

    @Insert("INSERT INTO CY_Folder (ParentFolderID, AncestorList, FolderName, FolderCode, PhysicalPath, " +
            "DisplayOrder, PersonInCharge, FolderStatus, UsageDescription, DeleteFlag, CreatedBy, CreatedAt, Remarks) " +
            "VALUES (#{parentFolderID}, #{ancestorList}, #{folderName}, #{folderCode}, #{physicalPath}, " +
            "#{displayOrder}, #{personInCharge}, #{folderStatus}, #{usageDescription}, #{deleteFlag}, " +
            "#{createdBy}, NOW(), #{remarks})")
    @Options(useGeneratedKeys = true, keyProperty = "folderID")
    void insertFolder(CYFolder folder);
}

