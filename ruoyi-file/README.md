下面给出一份针对上传接口的详细使用说明和测试文档示例，说明文档中包含接口描述、请求 URL、请求参数说明、请求示例以及 Postman 测试配置示例，供你参考。

---

# 上传接口使用说明

## 接口描述

该接口用于将文件上传到 FTP 服务器，并将相应的文件记录保存至数据库。上传时需要传入以下参数：

- **file**：上传的文件（文件类型），必填。
- **DocumentTypeID**：文档类型 ID（必填）。上传前，根据该参数通过内部逻辑转换为英文文档类型名称。内部允许的文档类型包括：
  - **1** 对应 *Material Physicochemical Report*（材质理化报告）
  - **2** 对应 *Certificate*（合格证）
  - **3** 对应 *Manual*（说明书）
  - **4** 对应 *Product Inspection Report*（产品检验报告）
  - **5** 对应 *Packing List*（装箱单）
  - **6** 对应 *Supplier Raw Material Report*（供应商原材料报告）
- **matchID**：匹配 ID（必填）。用于唯一标识对应的文件记录。
- **PlanTrackingNumber**：计划跟踪号（选填），若有计划跟踪信息可传入。

接口接收到请求后会：
1. 校验必填项是否为空。
2. 根据传入的 `DocumentTypeID` 调用内部方法 `getDocumentTypeName` 获取对应的英文文档类型名称，并与允许的文档类型列表 (`ALLOWED_DOCUMENT_TYPES`) 对比。如果非法，则返回错误信息。
3. 为文件生成唯一的时间戳和新文件名，并在本地临时目录保存文件。
4. 查找或创建对应的文件夹，再将文件上传至 FTP 服务器。
5. 构建文件 URL，并将相关文件记录（包括文件名、文档类型、matchID、时间戳、计划跟踪号等）保存至数据库。
6. 最后返回上传成功信息及文件 URL，或相应的错误提示。

---

## 请求 URL

```
[POST] http://<server-address>/fms/ftp/upload
```

*示例地址：*
```
http://10.11.0.20:8088/fms/ftp/upload
```

*注：请根据实际部署地址修改 `<server-address>`。*

---

## 请求参数说明

| 参数名             | 类型      | 必填 | 说明                                                         |
|------------------|---------|-----|------------------------------------------------------------|
| file             | File    | 是  | 要上传的文件。注意：使用 form-data 中的 file 类型上传。           |
| DocumentTypeID   | Integer | 是  | 文档类型 ID。系统内部通过该 ID 获取对应文档类型英文名称，例如 2 表示 Certificate（合格证）。 |
| matchID          | String | 是  | 匹配 ID，用于唯一标识该文件的记录。                                  |
| PlanTrackingNumber | String | 否  | 计划跟踪号，若有计划跟踪信息可传入（选填）。                          |

---

## 请求示例

**示例说明**：  
假设上传一个文件，`DocumentTypeID` 为 **2** （即 Certificate 合格证）、`matchID` 为 **123**，且计划跟踪号为 **PTN-20250114-001**。

**接口调用后成功返回示例：**

```json
{
  "code": 200,
  "msg": "文件上传成功并已存储到数据库",
  "url": "ftp://yourftpserver/path/to/uploaded/file_newFileName.ext"
}
```

如果传入的 `DocumentTypeID` 不符合要求或其他参数有误，则可能返回类似下面的错误提示：

```json
{
  "code": 500,
  "msg": "不支持的文档类型ID: 7"
}
```
---

## 使用说明

1. **导入 Collection**
  - 将上面的 JSON 内容复制到一个文件中，保存为 `FMS_FTP_Upload.postman_collection.json`。
  - 打开 Postman，点击左上角的 **Import**，选择该 JSON 文件导入。

2. **配置请求参数**
  - 在 Postman 中选择“文件上传接口”请求。
  - 在 `Body` → `form-data` 中，填写各个字段：
    - **file**：点击选择本地文件；
    - **DocumentTypeID**：填写对应的文档类型 ID（示例中使用 2 表示 Certificate 合格证）；
    - **matchID**：填写匹配 ID（例如 123）；
    - **PlanTrackingNumber**：可填写计划跟踪号（例如 PTN-20250114-001，可选）。

3. **修改请求 URL**
  - 如实际部署地址不一致，请修改请求 URL 中的 `http://10.11.0.20:8088` 为正确的服务器地址。

4. **发送请求测试**
  - 点击 **Send** 按钮提交请求，检查返回结果是否符合预期。

