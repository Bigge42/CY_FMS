┌── 文件服务器(E:)
│
├─ TC文件 ((E:\tcfile)
│   ├─ 图纸PDF (E:\tcfile\TZ)
│   └─ 工艺文件 (E:\tcfile\BOP)
│
├─ SMT文件 (E:\smtfile)
│   ├─ 图纸PDF (E:\smtfile\TZ)
│   └─ 资源文件 (E:\smtfile\sourcefile)

└─ 文件 (E:\files)
    ├─ 招标过程文档 (E:\files\zbgcfiles)
    │   ├─ 客户原始数据表 (E:\files\zbgcfiles\khysjsb)
    │   └─ 招标文件 (E:\files\zbgcfiles\zbwj)
    │
    ├─ 投标过程文档 (E:\files\tbgcfiles)
    │   ├─ 投标文件 (E:\files\tbgcfiles\tbwj)
    │   ├─ 选型报价表 (E:\files\tbgcfiles\xxbjb)
    │   ├─ 选型数据表 (E:\files\tbgcfiles\xxsjb)
    │   ├─ 尺寸图 (E:\files\tbgcfiles\cct)
    │   ├─ 气路图 (E:\files\tbgcfiles\qlt)
    │   ├─ 力矩表 (E:\files\tbgcfiles\ljb)
    │   ├─ 澄清表 (E:\files\tbgcfiles\cqb)
    │   ├─ 流量曲线图 (E:\files\tbgcfiles\llqxt)
    │   └─ 资质证书、营业执照等(E:\files\tbgcfiles\other)
    │
    ├─ 合同管理文档 (E:\files\htgfiles) 
    │   ├─  合同文档 (E:\files\htgfiles\htwd)
    │   └─  技术协议 (E:\files\htgfiles\jsxy)
    │
    ├─ 设计过程文档 (E:\files\scgcfiles)
    │   ├─ 计算书 (E:\files\scgcfiles\jss)
    │   └─ 说明书 (E:\files\scgcfiles\sms)
    │
    ├─ 供货过程文档 (E:\files\ghgcfiles)
    │   ├─ 材质提报报告 (E:\files\czlhbg)
    │   └─ 供应商原材料 (E:\files\gyyssycl)
    │
    └─ 生产过程文档 (E:\files\scgfiles)
    │   ├─ 喷涂报告 (E:\files\scgfiles\ptbg)
    │   ├─ 零件尺寸记录 (E:\files\scgfiles\ljccjl)
    │   ├─ 喷焊报告 (E:\files\scgfiles\phbbg)
    │   ├─ 热处理报告 (E:\files\scgfiles\rclbg)
    │   ├─ 焊接报告 (E:\files\scgfiles\hjbg)
    │   ├─ 产品检验报告 (E:\files\scgfiles\cpjybq)
    │   ├─ 装箱单 (E:\files\scgfiles\zxd)
    │   ├─ 封箱前拍照 (E:\files\scgfiles\fzqpz)  
    └─  └─ 合格证 (E:\files\scgfiles\hgz)


## 需求梳理

文档涉及以下主要分类（文档类型）：

1. 招标过程文档
2. 投标过程文档
3. 合同管理文档
4. 设计过程文档
5. 供货过程文档
6. 生产过程文档

以及与文档相关的基础属性字段（可根据需求补充或精简）：
- 文档类型 (document_type)
- 匹配ID (match_id)
- 文件名称 (document_name)
- 文件标签 (document_label)（如有需要，可存放关键字或分类标签）
- 上传时间 (upload_time)
- 修改时间 (modify_time)
- URL (url)

此外，还有与匹配ID相关的字段/键值：
- 项目ID (project_id)
- 销售合同 (sales_contract_no)
- 物料编码 (material_code)
- 送货单号 (delivery_note_no)
- 批次号 (batch_no)

## 数据库设计思路

### 1. 基础代码表/参考表设计

**DocumentTypes表** (存放文档类型的基础信息，方便扩展)：
```sql
CREATE TABLE DocumentTypes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL,
    type_code VARCHAR(50) NOT NULL UNIQUE
);
```
示例数据：
- (1, '招标过程文档', 'zbgcfiles')
- (2, '投标过程文档', 'tbgcfiles')
- (3, '合同管理文档', 'htgfiles')
- (4, '设计过程文档', 'scgcfiles')
- (5, '供货过程文档', 'ghgcfiles')
- (6, '生产过程文档', 'scgfiles')

### 2. 业务相关参考表（根据企业实际情况可建立）

根据字段“项目ID”、“销售合同”、“物料编码”等，如果您在系统中有对应的管理模块或基础数据表，建议将其规范化存储。例如：

- **Projects（项目表）**  
  存储项目基本信息（project_id, project_name, ...）

- **SalesContracts（销售合同表）**  
  存储销售合同的基础信息（contract_no, customer_id, contract_sign_date...）

- **Materials（物料表）**  
  存储物料的基础信息（material_code, material_description, ...）

- **Deliveries（送货单表）**  
  存储送货单信息（delivery_note_no, delivery_date, supplier_id...）

- **Batches（批次表）**  
  存储批次信息（batch_no, batch_description, ...）

实际情况中，可能有的字段不需要建立单独的表（如batch_no也许只是简单标识），这些可视您系统复杂度决定。

### 3. 核心文档表设计

建议建立一张统一的Documents表，用于存储所有文档的基础信息，然后通过文档类型字段与DocumentTypes表关联，通过可选外键字段（如project_id、sales_contract_no等）将其与业务数据表关联。这样，在后期扩展和检索时更灵活。

**Documents表** 示例结构：
```sql
CREATE TABLE Documents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    document_type_id INT NOT NULL,
    match_id VARCHAR(100),
    document_name VARCHAR(255),
    document_label VARCHAR(255),
    upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modify_time DATETIME NULL,
    url VARCHAR(500),

    project_id INT NULL,
    sales_contract_no VARCHAR(100),
    material_code VARCHAR(100),
    delivery_note_no VARCHAR(100),
    batch_no VARCHAR(100),

    FOREIGN KEY (document_type_id) REFERENCES DocumentTypes(id)
    -- 如果有对应的基础数据表则可以建立外键，如：
    -- FOREIGN KEY (project_id) REFERENCES Projects(id)
    -- FOREIGN KEY (sales_contract_no) REFERENCES SalesContracts(contract_no)
    -- FOREIGN KEY (material_code) REFERENCES Materials(material_code)
    -- ...依实际情况决定是否做外键关联
);
```

### 4. 数据填充与检索

- 当您上传/新增一个文档时，根据其所属的文档类别（如生产过程文档），将document_type_id填入对应的ID（例如生产过程文档对应`scgfiles`的ID=6）。
- 若此文档关联某个项目，则在project_id字段填入该项目在Projects表中的ID。
- 同理，对于销售合同、物料编码、送货单号、批次号等信息也同样填入。

### 5. 优化与扩展

- 如果文件标签（document_label）需要存放多个标签，可将其设计成一对多关系，如单独建立DocumentLabels表，通过document_id关联。
- 若url需要区分本地存储、云存储、版本控制等，也可建立相应的Version或FileStorage表细分管理。
- 为提升查询性能，可以根据实际查询需求对常用条件字段建立相应索引（如project_id、sales_contract_no、material_code上建立索引）。

## 总结

通过上述设计，您将拥有一套可扩展且清晰的数据库结构：
- 一张DocumentTypes表定义基础文档类型。
- 一张Documents表存放文档基础信息，以及与业务数据表的关联字段。
- 对于项目、合同、物料、送货、批次等信息，可根据企业系统规划决定是否建立对应参考表进行管理和关联。

