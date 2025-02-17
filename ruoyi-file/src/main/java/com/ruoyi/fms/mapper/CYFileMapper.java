package com.ruoyi.fms.mapper;

import com.ruoyi.fms.domain.CYFile;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CYFileMapper {

        @Insert("INSERT INTO CY_FILE(fileID,fileName, folderID, documentTypeID, matchID, documentTypeName, versionNumber, planTrackingNumber, createdBy, createdAt, fileURL) " +
                "VALUES(#{fileID},#{fileName}, #{folderID}, #{documentTypeID}, #{matchID}, #{documentTypeName}, #{versionNumber}, #{planTrackingNumber}, #{createdBy}, NOW(), #{fileURL})")
        @Options(useGeneratedKeys = true, keyProperty = "fileID")
        int insertFile(CYFile file);

        @Select("SELECT * FROM CY_FILE WHERE documentTypeName = #{documentTypeName} AND matchID = #{matchID} AND deleteFlag = 0")
        CYFile findByFileNameAndMatchID(@Param("documentTypeName") String documentTypeName, @Param("matchID") Integer matchID);

        @Update("UPDATE CY_FILE SET deleteFlag = 1, updatedAt = NOW() WHERE fileID = #{fileID}")
        int markAsDeleted(@Param("fileID") String fileID);

        @Select("SELECT * FROM CY_FILE WHERE fileID = #{fileId} AND deleteFlag = 0")
        CYFile selectByFileId(@Param("fileId") String fileId);
}
