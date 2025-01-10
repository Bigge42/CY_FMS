package com.ruoyi.fms.mapper;

import com.ruoyi.fms.domain.CYFolder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CYFolderMapper {

    @Select("SELECT * FROM CY_Folder WHERE folderName = #{folderName} AND deleteFlag = 0")
    CYFolder findByFolderName(@Param("folderName") String folderName);

    @Insert("INSERT INTO CY_Folder(folderName, folderCode, physicalPath, createdBy, createdAt) " +
            "VALUES(#{folderName}, #{folderCode}, #{physicalPath}, #{createdBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "folderID")
    int insertFolder(CYFolder folder);

    @Select("SELECT * FROM CY_Folder WHERE folderID = #{folderID} AND deleteFlag = 0")
    CYFolder findById(@Param("folderID") Integer folderID);
}
