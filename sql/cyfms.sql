-- ===========================================
-- 数据库：cyfms
-- ===========================================

-- 使用 InnoDB 引擎以支持事务
SET default_storage_engine=INNODB;

-- ===========================================
-- 删除旧表（如果存在）
-- ==========================================
DROP TABLE IF EXISTS CY_FILE;
DROP TABLE IF EXISTS CY_Folder;

-- ===========================================
-- 创建 CY_Folder 表
-- ===========================================
CREATE TABLE CY_Folder (
                           FolderID INT AUTO_INCREMENT PRIMARY KEY COMMENT '文件夹ID', -- 自增主键
                           ParentFolderID INT NULL COMMENT '父文件夹ID', -- 自引用父文件夹ID
                           AncestorList VARCHAR(255) NULL COMMENT '祖级列表', -- 可以存储路径或ID列表
                           FolderName VARCHAR(100) NOT NULL COMMENT '文件夹名称', --
                           FolderCode VARCHAR(50) NOT NULL UNIQUE COMMENT '文件夹代号', -- 唯一
                           PhysicalPath VARCHAR(255) NULL COMMENT '物理路径', --
                           DisplayOrder INT DEFAULT 0 COMMENT '显示顺序', --
                           PersonInCharge VARCHAR(100) NULL COMMENT '负责人', --
                           FolderStatus VARCHAR(50) NULL COMMENT '文件夹状态（如启用、禁用等）', --
                           UsageDescription VARCHAR(255) NULL COMMENT '用途描述', --
                           DeleteFlag TINYINT(1) DEFAULT 0 COMMENT '删除标志', -- 0表示正常，1表示已删除
                           CreatedBy VARCHAR(50) NOT NULL COMMENT '创建者', --
                           CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', --
                           UpdatedBy VARCHAR(50) NULL COMMENT '更新者', --
                           UpdatedAt DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', --
                           Remarks VARCHAR(255) NULL COMMENT '备注' --
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================
-- 创建 CY_FILE 合并表
-- ===========================================
CREATE TABLE CY_FILE (
                         FileID INT AUTO_INCREMENT NOT NULL COMMENT '文件ID', -- 文件ID作为主键
                         FileName VARCHAR(255) NOT NULL COMMENT '文件名',
                         FolderID INT NOT NULL COMMENT '所属文件夹ID',
                         DocumentTypeID INT NULL COMMENT '文档类型ID',
                         MatchID VARCHAR(50) NULL COMMENT '匹配ID',
                         DocumentTypeName VARCHAR(100) NOT NULL COMMENT '文档类型名称',
                         VersionNumber VARCHAR(50) NOT NULL COMMENT '版本号',
                         FileTag VARCHAR(100) NULL COMMENT '文件标签',
                         PlanTrackingNumber VARCHAR(100) NULL COMMENT '计划跟踪号',
                         DeleteFlag TINYINT(1) DEFAULT 0 COMMENT '删除标志', -- 0表示正常，1表示已删除
                         CreatedBy VARCHAR(50) NOT NULL COMMENT '创建者',
                         CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         UpdatedBy VARCHAR(50) NULL COMMENT '更新者',
                         UpdatedAt DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         Remarks VARCHAR(255) NULL COMMENT '备注',
                         FileURL VARCHAR(500) NULL COMMENT '文件URL',
                         PRIMARY KEY (FileID) -- 设置 FileID 为主键
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================
-- 索引与约束
-- ===========================================

-- 在 CY_FOLDER 表上创建索引
CREATE INDEX IDX_CY_FOLDER_FolderName ON CY_Folder(FolderName);
CREATE INDEX IDX_CY_FOLDER_PersonInCharge ON CY_Folder(PersonInCharge);
CREATE INDEX IDX_CY_FOLDER_FolderStatus ON CY_Folder(FolderStatus);

-- 在 CY_FILE 表上创建索引
CREATE INDEX IDX_CY_FILE_FileName ON CY_FILE(FileName);
CREATE INDEX IDX_CY_FILE_FolderID ON CY_FILE(FolderID);
CREATE INDEX IDX_CY_FILE_DocumentTypeID ON CY_FILE(DocumentTypeID);
CREATE INDEX IDX_CY_FILE_MatchID ON CY_FILE(MatchID);
CREATE INDEX IDX_CY_FILE_VersionNumber ON CY_FILE(VersionNumber);
