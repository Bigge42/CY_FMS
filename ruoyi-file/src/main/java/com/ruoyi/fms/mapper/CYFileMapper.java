package com.ruoyi.fms.mapper;

import com.ruoyi.fms.domain.CYFile;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CYFileMapper {

        @Insert("INSERT INTO CY_FILE(fileName, folderID, documentTypeID, matchID, documentTypeName, versionNumber, createdBy, createdAt, fileURL) " +
                "VALUES(#{fileName}, #{folderID}, #{documentTypeID}, #{matchID}, #{documentTypeName}, #{versionNumber}, #{createdBy}, NOW(), #{fileURL})")
        @Options(useGeneratedKeys = true, keyProperty = "fileID")
        int insertFile(CYFile file);

        @Select("SELECT * FROM CY_FILE WHERE fileName = #{fileName} AND matchID = #{matchID} AND deleteFlag = 0")
        CYFile findByFileNameAndMatchID(@Param("fileName") String fileName, @Param("matchID") Integer matchID);

        @Update("UPDATE CY_FILE SET deleteFlag = 1, updatedAt = NOW() WHERE fileID = #{fileID}")
        int markAsDeleted(@Param("fileID") Integer fileID);
}
