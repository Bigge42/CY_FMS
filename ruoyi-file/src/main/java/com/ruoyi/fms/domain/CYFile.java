package com.ruoyi.fms.domain;

import lombok.Data;
import java.sql.Date;

@Data
public class CYFile {
    private Integer fileID;            // 文件ID
    private String fileName;           // 文件名
    private Integer folderID;          // 所属文件夹ID
    private Integer documentTypeID;    // 文档类型ID
    private Integer matchID;           // 匹配ID
    private String documentTypeName;   // 文档类型名称
    private String versionNumber;      // 版本号
    private String fileTag;            // 文件标签
    private Integer deleteFlag;        // 删除标志
    private String createdBy;          // 创建者
    private Date createdAt;            // 创建时间
    private String updatedBy;          // 更新者
    private Date updatedAt;            // 更新时间
    private String remarks;            // 备注
    private String fileURL;            // 文件URL
}
