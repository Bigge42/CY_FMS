package com.ruoyi.fms.domain;

import lombok.Data;

import java.sql.Date;

@Data
public class CYFolder {
    private Integer folderID;          // 文件夹ID
    private Integer parentFolderID;    // 父文件夹ID
    private String ancestorList;       // 祖级列表
    private String folderName;         // 文件夹名称
    private String folderCode;         // 文件夹代号
    private String physicalPath;       // 物理路径
    private Integer displayOrder;      // 显示顺序
    private String personInCharge;     // 负责人
    private String folderStatus;       // 文件夹状态
    private String usageDescription;   // 用途描述
    private Integer deleteFlag;        // 删除标志
    private String createdBy;          // 创建者
    private Date createdAt;            // 创建时间
    private String updatedBy;          // 更新者
    private Date updatedAt;            // 更新时间
    private String remarks;            // 备注
}
