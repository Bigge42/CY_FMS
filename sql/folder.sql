DROP TABLE IF EXISTS cy_folder;
CREATE TABLE cy_folder (
                            folder_id       BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '文件夹ID',
                            parent_id       BIGINT(20)      DEFAULT 0               COMMENT '父文件夹ID',
                            ancestors       VARCHAR(200)    DEFAULT ''              COMMENT '祖级列表，例如0,200,201 用逗号分隔',
                            folder_name     VARCHAR(100)    NOT NULL                COMMENT '文件夹名称（如：图纸PDF, 工艺文件, SMT文件, ...）',
                            folder_code     VARCHAR(100)    DEFAULT NULL            COMMENT '文件夹代号（如：tcfile、TZ、smtfile、zbwj）',
                            absolute_path   VARCHAR(500)    DEFAULT NULL            COMMENT '对应物理路径(如：E:\\tcfile\\TZ)，可选',
                            order_num       INT(4)          DEFAULT 0               COMMENT '显示顺序',
                            status          CHAR(1)         DEFAULT '0'             COMMENT '文件夹状态（0正常 1停用）',
                            description     VARCHAR(500)    DEFAULT NULL            COMMENT '文件夹用途描述',
                            leader          VARCHAR(20)     DEFAULT NULL            COMMENT '负责人（可选）',
                            phone           VARCHAR(11)     DEFAULT NULL            COMMENT '联系电话（可选）',
                            email           VARCHAR(50)     DEFAULT NULL            COMMENT '邮箱（可选）',
                            del_flag        CHAR(1)         DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
                            create_by       VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
                            create_time     DATETIME                               COMMENT '创建时间',
                            update_by       VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
                            update_time     DATETIME                               COMMENT '更新时间',
                            remark          VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
                            PRIMARY KEY (folder_id)
) ENGINE=INNODB COMMENT='文件夹表';


INSERT INTO cy_folder (folder_id, parent_id, ancestors, folder_name, folder_code, absolute_path, order_num, status, create_by, create_time) VALUES
                                                                                                                                                  (1,0,'0','文件服务器(E:)','E_disk','E:\\',0,'0','admin',NOW()),
                                                                                                                                                  (2,1,'0,1','TC文件','tcfile','E:\\tcfile',1,'0','admin',NOW()),
                                                                                                                                                  (3,2,'0,1,2','图纸PDF','TZ','E:\\tcfile\\TZ',1,'0','admin',NOW()),
                                                                                                                                                  (4,2,'0,1,2','工艺文件','BOP','E:\\tcfile\\BOP',2,'0','admin',NOW()),
                                                                                                                                                  (5,1,'0,1','SMT文件','smtfile','E:\\smtfile',2,'0','admin',NOW()),
                                                                                                                                                  (6,5,'0,1,5','图纸PDF','TZ','E:\\smtfile\\TZ',1,'0','admin',NOW()),
                                                                                                                                                  (7,5,'0,1,5','资源文件','sourcefile','E:\\smtfile\\sourcefile',2,'0','admin',NOW()),
                                                                                                                                                  (8,1,'0,1','文件','files','E:\\files',3,'0','admin',NOW()),
                                                                                                                                                  (9,8,'0,1,8','招标过程文档','zbgcfiles','E:\\files\\zbgcfiles',1,'0','admin',NOW()),
                                                                                                                                                  (10,9,'0,1,8,9','客户原始数据表','khysjsb','E:\\files\\zbgcfiles\\khysjsb',1,'0','admin',NOW()),
                                                                                                                                                  (11,9,'0,1,8,9','招标文件','zbwj','E:\\files\\zbgcfiles\\zbwj',2,'0','admin',NOW()),
                                                                                                                                                  (12,8,'0,1,8','投标过程文档','tbgcfiles','E:\\files\\tbgcfiles',2,'0','admin',NOW()),
                                                                                                                                                  (13,12,'0,1,8,12','投标文件','tbwj','E:\\files\\tbgcfiles\\tbwj',1,'0','admin',NOW()),
                                                                                                                                                  (14,12,'0,1,8,12','选型报价表','xxbjb','E:\\files\\tbgcfiles\\xxbjb',2,'0','admin',NOW()),
                                                                                                                                                  (15,12,'0,1,8,12','选型数据表','xxsjb','E:\\files\\tbgcfiles\\xxsjb',3,'0','admin',NOW()),
                                                                                                                                                  (16,12,'0,1,8,12','尺寸图','cct','E:\\files\\tbgcfiles\\cct',4,'0','admin',NOW()),
                                                                                                                                                  (17,12,'0,1,8,12','气路图','qlt','E:\\files\\tbgcfiles\\qlt',5,'0','admin',NOW()),
                                                                                                                                                  (18,12,'0,1,8,12','力矩表','ljb','E:\\files\\tbgcfiles\\ljb',6,'0','admin',NOW()),
                                                                                                                                                  (19,12,'0,1,8,12','澄清表','cqb','E:\\files\\tbgcfiles\\cqb',7,'0','admin',NOW()),
                                                                                                                                                  (20,12,'0,1,8,12','流量曲线图','llqxt','E:\\files\\tbgcfiles\\llqxt',8,'0','admin',NOW()),
                                                                                                                                                  (21,12,'0,1,8,12','其他类文件','other','E:\\files\\tbgcfiles\\other',9,'0','admin',NOW()),
                                                                                                                                                  (22,8,'0,1,8','合同管理文档','htgfiles','E:\\files\\htgfiles',3,'0','admin',NOW()),
                                                                                                                                                  (23,22,'0,1,8,22','合同文档','htwd','E:\\files\\htgfiles\\htwd',1,'0','admin',NOW()),
                                                                                                                                                  (24,22,'0,1,8,22','技术协议','jsxy','E:\\files\\htgfiles\\jsxy',2,'0','admin',NOW()),
                                                                                                                                                  (25,8,'0,1,8','设计过程文档','scgcfiles','E:\\files\\scgcfiles',4,'0','admin',NOW()),
                                                                                                                                                  (26,25,'0,1,8,25','计算书','jss','E:\\files\\scgcfiles\\jss',1,'0','admin',NOW()),
                                                                                                                                                  (27,25,'0,1,8,25','说明书','sms','E:\\files\\scgcfiles\\sms',2,'0','admin',NOW()),
                                                                                                                                                  (28,8,'0,1,8','供货过程文档','ghgcfiles','E:\\files\\ghgcfiles',5,'0','admin',NOW()),
                                                                                                                                                  (29,28,'0,1,8,28','材质提报报告','czlhbg','E:\\files\\ghgcfiles\\czlhbg',1,'0','admin',NOW()),
                                                                                                                                                  (30,28,'0,1,8,28','供应商原材料','gyyssycl','E:\\files\\ghgcfiles\\gyyssycl',2,'0','admin',NOW()),
                                                                                                                                                  (31,8,'0,1,8','生产过程文档','scgfiles','E:\\files\\scgfiles',6,'0','admin',NOW()),
                                                                                                                                                  (32,31,'0,1,8,31','喷涂报告','ptbg','E:\\files\\scgfiles\\ptbg',1,'0','admin',NOW()),
                                                                                                                                                  (33,31,'0,1,8,31','零件尺寸记录','ljccjl','E:\\files\\scgfiles\\ljccjl',2,'0','admin',NOW()),
                                                                                                                                                  (34,31,'0,1,8,31','喷焊报告','phbbg','E:\\files\\scgfiles\\phbbg',3,'0','admin',NOW()),
                                                                                                                                                  (35,31,'0,1,8,31','热处理报告','rclbg','E:\\files\\scgfiles\\rclbg',4,'0','admin',NOW()),
                                                                                                                                                  (36,31,'0,1,8,31','焊接报告','hjbg','E:\\files\\scgfiles\\hjbg',5,'0','admin',NOW()),
                                                                                                                                                  (37,31,'0,1,8,31','产品检验报告','cpjybq','E:\\files\\scgfiles\\cpjybq',6,'0','admin',NOW()),
                                                                                                                                                  (38,31,'0,1,8,31','装箱单','zxd','E:\\files\\scgfiles\\zxd',7,'0','admin',NOW()),
                                                                                                                                                  (39,31,'0,1,8,31','封箱前拍照','fzqpz','E:\\files\\scgfiles\\fzqpz',8,'0','admin',NOW()),
                                                                                                                                                  (40,31,'0,1,8,31','合格证','hgz','E:\\files\\scgfiles\\hgz',9,'0','admin',NOW());
DROP TABLE IF EXISTS cy_file;
CREATE TABLE cy_file (
                          file_id              BIGINT(20)      NOT NULL AUTO_INCREMENT COMMENT '文件ID',
                          folder_id            BIGINT(20)      NOT NULL                COMMENT '所属文件夹ID',
                          document_type        VARCHAR(50)     NOT NULL                COMMENT '文档类型（如：招标过程文档、投标过程文档等）',
                          match_id             VARCHAR(100)    DEFAULT NULL            COMMENT '匹配ID，用于外部业务数据关联',
                          document_name        VARCHAR(255)    NOT NULL                COMMENT '文件名称',
                          document_label       VARCHAR(255)    DEFAULT NULL            COMMENT '文件标签或关键字',
                          upload_time          DATETIME        NOT NULL                COMMENT '文件上传时间',
                          modify_time          DATETIME        DEFAULT NULL            COMMENT '文件信息修改时间',
                          url                  VARCHAR(500)    DEFAULT NULL            COMMENT '文件访问URL或存储路径',
                          project_id           VARCHAR(100)    DEFAULT NULL            COMMENT '项目ID',
                          sales_contract_no    VARCHAR(100)    DEFAULT NULL            COMMENT '销售合同号',
                          material_code        VARCHAR(100)    DEFAULT NULL            COMMENT '物料编码',
                          delivery_note_no     VARCHAR(100)    DEFAULT NULL            COMMENT '送货单号',
                          batch_no             VARCHAR(100)    DEFAULT NULL            COMMENT '批次号',
                          create_by            VARCHAR(64)     DEFAULT ''              COMMENT '创建者',
                          create_time          DATETIME                               COMMENT '创建时间',
                          update_by            VARCHAR(64)     DEFAULT ''              COMMENT '更新者',
                          update_time          DATETIME                               COMMENT '更新时间',
                          del_flag             CHAR(1)         DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
                          PRIMARY KEY (file_id),
                          KEY idx_file_folder (folder_id),
                          CONSTRAINT fk_file_folder FOREIGN KEY (folder_id) REFERENCES cy_folder (folder_id)
) ENGINE=INNODB COMMENT='文件信息表';

INSERT INTO cy_file (
    folder_id, document_type, match_id, document_name, document_label, upload_time, modify_time, url, project_id, sales_contract_no, material_code, delivery_note_no, batch_no, create_by, create_time, update_by, update_time, del_flag
) VALUES
      (9,  '招标过程文档', 'PROJECT_001', '客户原始数据表2024版.pdf', '客户数据,原始', NOW(), NULL, 'http://fileserver/files/zbgcfiles/khysjsb/客户原始数据表2024版.pdf', 'PRJ_1001', 'SCN_20240901', 'MAT_001', 'DN_12345', 'BATCH_A1', 'admin', NOW(), '', NULL, '0'),

      (14, '投标过程文档', 'PROJECT_002', '选型报价表_v2.xlsx',   '报价,选型', NOW(), NULL, 'http://fileserver/files/tbgcfiles/xxbjb/选型报价表_v2.xlsx', 'PRJ_1002', 'SCN_20240902', 'MAT_002', 'DN_67890', 'BATCH_B2', 'admin', NOW(), '', NULL, '0'),

      (20, '投标过程文档', 'PROJECT_002', '流量曲线图_最终版.png', '流量,曲线', NOW(), NULL, 'http://fileserver/files/tbgcfiles/llqxt/流量曲线图_最终版.png', 'PRJ_1002', 'SCN_20240902', 'MAT_002', 'DN_67890', 'BATCH_B2', 'admin', NOW(), '', NULL, '0'),

      (23, '合同管理文档', 'PROJECT_001', '合同文档_签订版.docx',  '合同,签订', NOW(), NULL, 'http://fileserver/files/htgfiles/htwd/合同文档_签订版.docx', 'PRJ_1001', 'SCN_20240901', 'MAT_001', 'DN_12345', 'BATCH_A1', 'admin', NOW(), '', NULL, '0'),

      (37, '生产过程文档', 'PROJECT_003', '产品检验报告_合格.pdf', '检验,产品', NOW(), NULL, 'http://fileserver/files/scgfiles/cpjybq/产品检验报告_合格.pdf', 'PRJ_1003', 'SCN_20240903', 'MAT_010', 'DN_55555', 'BATCH_C3', 'admin', NOW(), '', NULL, '0');
