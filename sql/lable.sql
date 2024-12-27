-- 设置使用的字符集和存储引擎
SET NAMES utf8mb4;
SET SESSION sql_mode = 'STRICT_ALL_TABLES';

-- 禁用外键检查，以防止在删除表时遇到外键约束问题
SET FOREIGN_KEY_CHECKS = 0;

-- 删除表（按照外键依赖的逆序）
DROP TABLE IF EXISTS label_InspectionReport;
DROP TABLE IF EXISTS label_PhysicalChemicalReport;
DROP TABLE IF EXISTS label_ProductParameter;
DROP TABLE IF EXISTS label_Certificate;
DROP TABLE IF EXISTS label_PackingPhoto;
DROP TABLE IF EXISTS label_Manual;
DROP TABLE IF EXISTS label_DeliveryDoc;
DROP TABLE IF EXISTS label_MaterialReport;
DROP TABLE IF EXISTS label_hd_product;
DROP TABLE IF EXISTS label_Contract;
DROP TABLE IF EXISTS label_hd_supplier;

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 创建表

-- 1. 供应商信息表 (label_hd_supplier)
CREATE TABLE label_hd_supplier (
                                   SupplierID BIGINT PRIMARY KEY COMMENT '供应商唯一标识',
                                   supplier_name VARCHAR(255) NOT NULL COMMENT '供应商名称',
                                   supplier_num VARCHAR(255) NOT NULL COMMENT '供应商编码',
                                   create_time DATETIME COMMENT '创建时间',
                                   supplier_desc VARCHAR(255) COMMENT '备注信息',
                                   create_user_id BIGINT COMMENT '创建人内码',
                                   create_user_name VARCHAR(255) COMMENT '创建人',
                                   update_time DATETIME COMMENT '更新时间',
                                   update_user_id BIGINT COMMENT '更新人内码',
                                   update_user_name VARCHAR(255) COMMENT '更新人',
                                   del_flag TINYINT(1) DEFAULT 0 COMMENT '软删除标记',
                                   template_id BIGINT COMMENT '模版关联内码',
                                   INDEX (supplier_num) COMMENT '供应商编码索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商信息表';

-- 2. 原材料报告表 (label_MaterialReport)
CREATE TABLE label_MaterialReport (
                                      ReportID BIGINT PRIMARY KEY COMMENT '报告唯一标识',
                                      SupplierID BIGINT NOT NULL COMMENT '供应商ID（关联label_hd_supplier）',
                                      HeatNumber VARCHAR(255) NOT NULL UNIQUE COMMENT '炉号',
                                      MaterialCode VARCHAR(255) NOT NULL COMMENT '物料编码',
                                      DownloadLink VARCHAR(255) COMMENT '下载链接',
                                      BrowseLinks VARCHAR(255) COMMENT '浏览链接',
                                      creat_time DATETIME COMMENT '创建时间',
                                      FOREIGN KEY (SupplierID) REFERENCES label_hd_supplier(SupplierID)
                                          ON UPDATE CASCADE
                                          ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原材料报告表';

-- 3. 合同信息表 (label_Contract)
CREATE TABLE label_Contract (
                                ContractID INT PRIMARY KEY AUTO_INCREMENT COMMENT '合同唯一标识',
                                ContractCode VARCHAR(50) NOT NULL UNIQUE COMMENT '合同编码',
                                ContractDate DATE COMMENT '合同日期',
                                ContractName VARCHAR(255) COMMENT '合同名称',
                                CustomerName VARCHAR(255) COMMENT '客户名称',
                                INDEX (ContractCode) COMMENT '合同编码索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同信息表';

-- 4. 产品信息表 (label_hd_product)
CREATE TABLE label_hd_product (
                                  ID BIGINT PRIMARY KEY COMMENT '产品唯一标识 标签系统ID',
                                  product_type VARCHAR(255) NOT NULL COMMENT '产品型号',
                                  ContractID INT COMMENT '合同ID（关联合同信息表）',
                                  product_num VARCHAR(255) NOT NULL UNIQUE COMMENT '产品编号 唯一码',
                                  product_category VARCHAR(255) COMMENT '阀门类型',
                                  design_no VARCHAR(255) COMMENT '设计位号',
                                  gctj VARCHAR(255) COMMENT '公称通径',
                                  gcyl VARCHAR(255) COMMENT '公称压力',
                                  ftcz VARCHAR(255) COMMENT '阀体材质',
                                  zxjg VARCHAR(255) COMMENT '执行机构',
                                  zyxs VARCHAR(255) COMMENT '作用形式',
                                  qyyl VARCHAR(255) COMMENT '气源压力/电压',
                                  gzwd VARCHAR(255) COMMENT '工作温度',
                                  zzrq DATE COMMENT '出厂日期',
                                  xc VARCHAR(255) COMMENT '行程/流量特性/Cy',
                                  create_time DATETIME COMMENT '创建时间',
                                  create_user_id BIGINT COMMENT '创建人内码',
                                  create_user_name VARCHAR(255) COMMENT '创建人',
                                  update_time DATETIME COMMENT '更新时间',
                                  update_user_name VARCHAR(255) COMMENT '更新人',
                                  update_user_id BIGINT COMMENT '更新人内码',
                                  del_flag TINYINT(1) DEFAULT 0 COMMENT '软删除标记',
                                  request_id VARCHAR(50) COMMENT '请求标记',
                                  fxcz VARCHAR(255) COMMENT '阀芯材质',
                                  fzcz VARCHAR(255) COMMENT '阀座材质',
                                  lltx VARCHAR(255) COMMENT '流量特性',
                                  cv VARCHAR(255) COMMENT 'cv值',
                                  dy VARCHAR(255) COMMENT 'dy',
                                  factory_name VARCHAR(255) COMMENT '生产商名称',
                                  mplx VARCHAR(255) COMMENT '铭牌类型',
                                  FOREIGN KEY (ContractID) REFERENCES label_Contract(ContractID)
                                      ON UPDATE CASCADE
                                      ON DELETE SET NULL,
                                  INDEX (product_num) COMMENT '产品编号索引',
                                  INDEX (product_type) COMMENT '产品型号索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品信息表';

-- 5. 用料清单表 (label_ProductParameter)
CREATE TABLE label_ProductParameter (
                                        ID BIGINT PRIMARY KEY COMMENT '参数唯一标识',
                                        ProductID BIGINT NOT NULL COMMENT '产品ID（关联label_hd_product）',
                                        SelModelId BIGINT COMMENT '选择的模型ID',
                                        ERPPlanOrderId BIGINT COMMENT 'ERP计划号',
                                        Material_Code VARCHAR(255) COMMENT '物料编码',
                                        Material_Name VARCHAR(255) COMMENT '物料名称',
                                        Specification VARCHAR(255) COMMENT '规格型号',
                                        HeatNumber VARCHAR(255) COMMENT '炉号（关联label_MaterialReport，暂不行关联label_PhysicalChemicalReport）',
                                        FOREIGN KEY (ProductID) REFERENCES label_hd_product(ID)
                                            ON UPDATE CASCADE
                                            ON DELETE CASCADE,
                                        FOREIGN KEY (HeatNumber) REFERENCES label_MaterialReport(HeatNumber)
                                            ON UPDATE CASCADE
                                            ON DELETE CASCADE,
                                        INDEX (Material_Code) COMMENT '物料编码索引',
                                        INDEX (HeatNumber) COMMENT '炉号索引，（关联label_MaterialReport）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用料清单表';

-- 6. 合格证表 (label_Certificate)
CREATE TABLE label_Certificate (
                                   CertificateID INT PRIMARY KEY AUTO_INCREMENT COMMENT '合格证唯一标识',
                                   ProductID BIGINT NOT NULL COMMENT '产品ID（关联label_hd_product.ID）',
                                   IssueDate DATE COMMENT '日期',
                                   DownloadLink VARCHAR(255) COMMENT '下载链接',
                                   BrowseLinks VARCHAR(255) COMMENT '浏览链接',
                                   FOREIGN KEY (ProductID) REFERENCES label_hd_product(ID)
                                       ON UPDATE CASCADE
                                       ON DELETE CASCADE,
                                   INDEX (ProductID) COMMENT '产品ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合格证表';

-- 7. 产品检验报告表 (label_InspectionReport)
CREATE TABLE label_InspectionReport (
                                        InspectionID INT PRIMARY KEY AUTO_INCREMENT COMMENT '检验报告唯一标识',
                                        ProductID INT NOT NULL COMMENT '合格证ID（关联label_Certificate）',
                                        InspectionDetails TEXT COMMENT '检验详情',
                                        InspectionDate DATE COMMENT '检验日期',
                                        DownloadLink VARCHAR(255) COMMENT '下载链接',
                                        BrowseLinks VARCHAR(255) COMMENT '浏览链接',
                                        FOREIGN KEY (ProductID) REFERENCES label_Certificate(CertificateID)
                                            ON UPDATE CASCADE
                                            ON DELETE CASCADE,
                                        INDEX (ProductID) COMMENT '合格证ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品检验报告表';

-- 8. 材质理化报告表 (label_PhysicalChemicalReport)
CREATE TABLE label_PhysicalChemicalReport (
                                              PhysChemID INT PRIMARY KEY AUTO_INCREMENT COMMENT '报告唯一标识',
                                              HeatNumber VARCHAR(255) NOT NULL COMMENT '炉号（关联label_MaterialReport）',
                                              ReportDetails TEXT COMMENT '报告详情',
                                              ReportDate DATE COMMENT '报告日期',
                                              DownloadLink VARCHAR(255) COMMENT '下载链接',
                                              BrowseLinks VARCHAR(255) COMMENT '浏览链接',
                                              FOREIGN KEY (HeatNumber) REFERENCES label_MaterialReport(HeatNumber)
                                                  ON UPDATE CASCADE
                                                  ON DELETE CASCADE,
                                              INDEX (HeatNumber) COMMENT '炉号索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='材质理化报告表';

-- 9. 现场装箱照片管理表 (label_PackingPhoto)
CREATE TABLE label_PackingPhoto (
                                    PhotoID INT PRIMARY KEY AUTO_INCREMENT COMMENT '照片唯一标识',
                                    ProductID BIGINT NOT NULL COMMENT '产品ID（关联label_hd_product）',
                                    PhotoPath VARCHAR(255) NOT NULL COMMENT '照片存储路径',
                                    UploadDate DATE COMMENT '上传日期',
                                    DownloadLink VARCHAR(255) COMMENT '下载链接',
                                    BrowseLinks VARCHAR(255) COMMENT '浏览链接',
                                    FOREIGN KEY (ProductID) REFERENCES label_hd_product(ID)
                                        ON UPDATE CASCADE
                                        ON DELETE CASCADE,
                                    INDEX (ProductID) COMMENT '产品ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='现场装箱照片管理表';

-- 10. 说明书表 (label_Manual)
CREATE TABLE label_Manual (
                              ManualID INT PRIMARY KEY AUTO_INCREMENT COMMENT '说明书唯一标识',
                              product_type BIGINT NOT NULL COMMENT '产品ID（关联label_hd_product.ID）',
                              ManualPath VARCHAR(255) NOT NULL COMMENT '说明书存储路径',
                              UploadDate DATE COMMENT '上传日期',
                              DownloadLink VARCHAR(255) COMMENT '下载链接',
                              BrowseLinks VARCHAR(255) COMMENT '浏览链接',
                              FOREIGN KEY (product_type) REFERENCES label_hd_product(ID)
                                  ON UPDATE CASCADE
                                  ON DELETE CASCADE,
                              INDEX (product_type) COMMENT '产品ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='说明书表';

-- 11. 交工资料表 (label_DeliveryDoc)
CREATE TABLE label_DeliveryDoc (
                                   DeliveryID INT PRIMARY KEY AUTO_INCREMENT COMMENT '交工资料唯一标识',
                                   ProductID BIGINT NOT NULL COMMENT '产品ID（关联label_hd_product.ID）',
                                   DeliveryPath VARCHAR(255) NOT NULL COMMENT '交工资料存储路径',
                                   DeliveryDate DATE COMMENT '交工日期',
                                   DownloadLink VARCHAR(255) COMMENT '下载链接',
                                   BrowseLinks VARCHAR(255) COMMENT '浏览链接',
                                   FOREIGN KEY (ProductID) REFERENCES label_hd_product(ID)
                                       ON UPDATE CASCADE
                                       ON DELETE CASCADE,
                                   INDEX (ProductID) COMMENT '产品ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交工资料表';
