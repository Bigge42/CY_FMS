package com.ruoyi.fms.mapper;

import com.ruoyi.fms.domain.CYFile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper {
        @Insert("INSERT INTO CY_FILE (FileName, FolderID, DocumentTypeID, MatchID, DocumentTypeName, " +
                "VersionNumber, FileTag, DeleteFlag, CreatedBy, FileURL, CreatedAt, Remarks) " +
                "VALUES (#{fileName}, #{folderID}, #{documentTypeID}, #{matchID}, #{documentTypeName}, " +
                "#{versionNumber}, #{fileTag}, #{deleteFlag}, #{createdBy}, #{fileURL}, NOW(), #{remarks})")
        void insertFile(CYFile file);
}

