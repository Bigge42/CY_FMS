

---

# 文件ID与文档类型白名单

## 1. 方案概述

本方案用于在文件上传过程中自动生成唯一的文件ID。文件ID由两部分组成：
1. **文档类型缩写**：根据文件的文档类型ID映射得到一个固定的英文缩写。
2. **时间戳**：采用当前时间的毫秒级时间戳，确保文件ID的唯一性和一定的排序性。

组合后的文件ID格式为：
```
[文档类型缩写] + [时间戳]
```
例如，对于文档类型ID为 1（对应 “Material Physicochemical Report”，缩写为 "MPR"），在 2025 年 2 月 17 日 12:30:45.123 上传的文件，其生成的文件ID可能为：
```
MPR20250217123045123
```

---

## 2. 详细设计

### 2.1 文档类型缩写映射

文档类型ID与文档类型名称和缩写的对应关系如下：
下面是在前面生成的接口调用文档基础上，添加了中文备注及文档类型说明的完整文档示例。文档中包括了各接口的中文说明、请求示例、响应示例以及详细的文档类型对照表。

---

# 文件管理服务 API 调用文档

该服务用于文件上传、转换、下载、预览、删除等操作。所有接口均位于路径 `/fms/ftp` 下，部分接口允许匿名访问（使用 `@Anonymous` 注解）。



## 文档类型对照表

| 文档类型ID | 文档类型名称（英文）                  | 缩写 | 备注说明               |
| ---------- | ------------------------------------- | ---- | ---------------------- |
| 1          | Material Physicochemical Report       | MPR  | 材质理化报告           |
| 2          | Certificate                           | CER  | 合格证                 |
| 3          | Manual                                | MAN  | 说明书                 |
| 4          | Product Inspection Report             | PIR  | 产品检验报告           |
| 5          | Packing List                          | PL   | 装箱单                 |
| 6          | Supplier Raw Material Report          | SRM  | 供应商原材料报告       |
| 7          | Packing Photo                         | PP   | 装箱单照片             |
| 8          | Welding Report                        | WR   | 焊接报告               |
| 9          | Part Dimension Record Report          | PDR  | 零件尺寸记录报告       |
| 10         | Heat Treatment Report                 | HTR  | 热处理报告             |
| 11         | Spraying Report                       | SPR  | 喷涂报告               |
| 12         | Spray Welding Report                  | SWR  | 喷焊报告               |



映射关系可以通过 `switch` 语句或者映射表（例如 `Map<Integer, String>`）来实现。如果传入的文档类型ID不存在对应的映射，则默认返回 "UNK"（未知）。

### 2.2 时间戳生成

采用 Java 内置的 `SimpleDateFormat` 格式化当前时间，格式为 `"yyyyMMddHHmmssSSS"`：
- `yyyy` 表示年份
- `MM` 表示月份
- `dd` 表示日期
- `HH` 表示小时（24小时制）
- `mm` 表示分钟
- `ss` 表示秒
- `SSS` 表示毫秒

这种格式能确保在大部分情况下生成的时间戳唯一，并具有时间顺序性。例如，`20250217123045123` 表示 2025-02-17 12:30:45.123。

### 2.3 文件ID生成逻辑

生成文件ID的步骤如下：

1. **获取文档类型缩写**  
   根据传入的文档类型ID调用映射方法得到对应的缩写字符串（如 "MPR"）。

2. **生成时间戳**  
   使用 `SimpleDateFormat("yyyyMMddHHmmssSSS")` 获取当前时间的字符串表示，确保精确到毫秒。

3. **拼接生成文件ID**  
   将文档类型缩写与时间戳拼接，形成最终文件ID。例如：
   ```java
   String fileID = abbreviation + timestamp;
   ```
   其中 `abbreviation` 为文档类型缩写，`timestamp` 为生成的时间戳字符串。


---

# 文件管理服务 API 调用文档

该服务用于文件上传、转换、下载、预览、删除等操作。所有接口均在路径 `/fms/ftp` 下，部分接口允许匿名访问（`@Anonymous`）。


---

## 1. 文件上传接口

### 1.1 直接上传文件


**接口地址**
```
POST http://10.11.0.20:8088/fms/ftp/upload
```

**接口描述**  
直接上传文件到 FTP 服务器，不进行格式转换。上传成功后，将文件记录存入数据库，并返回生成的文件ID。

**请求参数** (Content-Type: multipart/form-data)

| 参数名称              | 类型          | 是否必填 | 说明                                          |
|-----------------------|---------------|----------|-----------------------------------------------|
| file                  | MultipartFile | 是       | 上传的文件。                                   |
| DocumentTypeID        | Integer       | 是       | 文档类型ID，必须在允许的文档类型列表内。         |
| matchID               | String        | 是       | 业务匹配ID，用于关联业务数据。                 |
| PlanTrackingNumber    | String        | 否       | 计划跟踪号（可选）。                           |

**示例请求 (curl)**
```bash
curl -X POST "http://localhost:8088/fms/ftp/upload" \
  -F "file=@/path/to/your/file.png" \
  -F "DocumentTypeID=1" \
  -F "matchID=12345" \
  -F "PlanTrackingNumber=PTN001"
```

**响应示例** (JSON)
```json
{
  "code": 200,
  "msg": "文件上传成功并已存储到数据库",
  "data": "MPR20250217151544001"
}
```

---

### 1.2 上传并转换为 PDF

**接口地址**
```
POST http://10.11.0.20:8088/fms/ftp/uploadToPdf
```

**接口描述**  
上传文件后先将文件转换为 PDF 格式，再上传到 FTP 服务器。转换完成后，将 PDF 文件记录存入数据库，并返回生成的文件ID。

**请求参数** (Content-Type: multipart/form-data)

| 参数名称              | 类型          | 是否必填 | 说明                                          |
|-----------------------|---------------|----------|-----------------------------------------------|
| file                  | MultipartFile | 是       | 上传的原始文件数据流。                         |
| DocumentTypeID        | Integer       | 是       | 文档类型ID，必须在允许的文档类型列表内。         |
| matchID               | String        | 是       | 业务匹配ID，用于关联业务数据。                 |
| PlanTrackingNumber    | String        | 否       | 计划跟踪号（可选）。                           |

**示例请求 (curl)**
```bash
curl -X POST "http://localhost:8088/fms/ftp/uploadToPdf" \
  -F "file=@/path/to/your/textfile.txt" \
  -F "DocumentTypeID=3" \
  -F "matchID=54321"
```

**响应示例** (JSON)
```json
{
  "code": 200,
  "msg": "文件上传转换为 PDF 成功并已存储到数据库",
  "data": "MAN20250217151612345"
}
```

---

## 2. 文件下载与预览接口

> **说明**  
> 客户端只需传入文件ID，后台会根据文件ID从数据库中查找文件存储路径，再将文件流输出给客户端。
> - **下载接口**：响应头设置为附件下载 (`Content-Disposition: attachment`)
> - **预览接口**：响应头设置为内联预览 (`Content-Disposition: inline`)

### 2.1 文件下载

**接口地址**
```
GET http://10.11.0.20:8088/fms/ftp/download
```

**接口描述**  
根据传入的文件ID，从数据库中查找文件存储路径，下载文件作为附件。如果文件不存在，则返回 HTTP 404。

**请求参数** (Query String)

| 参数名称 | 类型   | 是否必填 | 说明                         |
|----------|--------|----------|------------------------------|
| fileId   | String | 是       | 文件ID，用于查找对应文件记录。 |

**示例请求 (curl)**
```bash
curl -X GET "http://10.11.0.20:8088/fms/ftp/download?fileId=MPR20250217151544001" -o downloaded_file.png
```

**响应说明**
- 响应内容为二进制流，浏览器根据 `Content-Disposition: attachment` 头提示下载。
- 未查询到文件时返回 HTTP 状态码 `404`。

---

### 2.2 文件在线预览

**接口地址**
```
GET http://10.11.0.20:8088/fms/ftp/preview
```

**接口描述**  
根据传入的文件ID，从数据库中查找文件存储路径，直接将文件以内联方式输出，便于在浏览器中在线预览。

**请求参数** (Query String)

| 参数名称 | 类型   | 是否必填 | 说明                         |
|----------|--------|----------|------------------------------|
| fileId   | String | 是       | 文件ID，用于查找对应文件记录。 |

**示例请求 (curl)**
```bash
curl -X GET "http://10.11.0.20:8088/fms/ftp/preview?fileId=MAN20250217151612345" -o preview_file.pdf
```

**响应说明**
- 响应内容为二进制流，浏览器根据 `Content-Disposition: inline` 头直接预览文件（例如图片或 PDF）。
- 未查询到文件时返回 HTTP 状态码 `404`。

---

### 2.3 获取 SMT 文件 (下载 PDF)

**接口地址**
```
GET http://10.11.0.20:8088/fms/ftp/getSmtFile
```

**接口描述**  
根据传入的 SMT 文件名（不区分大小写），追加后缀 `.pdf`（如果缺失），从 FTP 服务器下载 SMT 文件到本地临时目录后输出文件流。下载完成后删除临时文件。

**请求参数** (Query String)

| 参数名称 | 类型   | 是否必填 | 说明                                        |
|----------|--------|----------|---------------------------------------------|
| smtfile  | String | 是       | SMT 文件名（可以不带 `.pdf` 后缀，会自动追加）。 |

**示例请求 (curl)**
```bash
curl -X GET "http://10.11.0.20:8088/fms/ftp/getSmtFile?smtfile=report" -o smt_report.pdf
```

**响应说明**
- 响应头中设置 `Content-Type: application/pdf` 及 `Content-Disposition: attachment`。
- 文件下载失败时返回对应的错误信息或 HTTP 状态码。



- **身份认证**  
  接口均使用 `@Anonymous` 注解，允许匿名访问。如果需要鉴权，请在请求中添加相应的认证信息。

- **错误处理**
  - 上传、转换、下载等操作出现异常时，接口会返回 `code` 为非 200 的响应。
  - 下载、预览接口中若未查到对应文件记录，则返回 HTTP 404。

- **响应格式**  
  所有接口统一使用内部 `Response` 类进行响应封装，结构如下：
  ```json
  {
      "code": <int>,
      "msg": "<提示信息>",
      "data": "<数据内容，通常为文件ID>"
  }
  ```

- **文件转换说明**
  - 上传至 PDF 接口使用 Apache PDFBox 将文本内容转换为 PDF，示例中仅适用于纯文本文件。
  - 对于其它文件格式，请引入相应的工具库实现正确的格式转换。
---