
---

# FMS FTP 接口文档

> **说明：**  
> 本接口前缀为 `http://localhost:8088/fms/ftp`  
> 允许上传的文档类型包括：`材质理化报告`、`合格证`、`说明书`、`产品检验报告`、`装箱单`、`供应商原材料报告`、`abc` 等。

---

## 1. 文件上传接口

**URL**
```
POST http://localhost:8088/fms/ftp/upload
```

**请求参数**
- **form-data** 类型参数：
    - `file` (file, 必传)：上传的文件内容。
    - `documentTypeName` (string, 必传)：文档类型名称。必须为允许的文档类型之一，例如 `"合格证"`。
    - `matchID` (number, 必传)：匹配 ID，用于文件记录的关联。

**请求说明**
- 上传接口会：
    1. 保存上传文件到本地临时目录；
    2. 根据 `documentTypeName` 查找或创建对应文件夹记录；
    3. 调用 FTP 服务上传文件，目标目录由文件夹记录的物理路径构成，生成相对路径格式，如： `"uploads/合格证/xxx_20250121155010.pdf"`；
    4. 将上传成功后的相对路径（如 `"uploads/合格证/xxx_20250121155010.pdf"`) 写入数据库文件记录；
    5. 返回 JSON 响应时额外返回 `url` 字段，值为上述相对路径，不包含 FTP 登录信息。

**示例请求**  
在 Postman 中设置 Body 类型为 `form-data`：

| Key               | Type    | Value                          |
|-------------------|---------|--------------------------------|
| file              | File    | 选择本地文件                   |
| documentTypeName  | Text    | 合格证                         |
| matchID           | Text    | 123                            |

**示例响应（成功）**
```json
{
  "code": 200,
  "msg": "文件上传成功并已存储到数据库",
  "url": "uploads/合格证/xxx_20250121155010.pdf"
}
```

**示例响应（失败）**
```json
{
  "code": 500,
  "msg": "文件上传到 FTP 失败"
}
```

---

## 2. 文件下载接口（返回本地存储路径）

**URL**
```
GET http://localhost:8088/fms/ftp/download
```

**请求参数（Query 参数）**
- `filePath` (string, 必传)：文件在 FTP 上的相对路径，例如：`uploads/合格证/file.txt`

**请求说明**
- 接口流程：
    1. 根据提供的 `filePath` 拆分出远程文件夹和文件名；
    2. 调用 FTP 下载方法，将文件保存到本地临时目录（路径由 FTP 配置中的临时目录和文件名拼接生成）；
    3. 返回 JSON 响应，提示文件下载成功与否，并返回本地文件保存路径。

**示例请求**
```
GET http://localhost:8088/fms/ftp/download?filePath=uploads/合格证/file.txt
```

**示例响应（成功）**
```json
{
  "code": 200,
  "msg": "文件下载成功，已保存到本地",
  "localPath": "/tmp/file.txt"
}
```

**示例响应（失败）**
```json
{
  "code": 500,
  "msg": "文件下载失败，请确认远程路径是否正确或FTP是否可用"
}
```

---

## 3. 文件下载接口（直接输出文件流）

> **说明：**  
> 如果希望浏览器直接弹出文件下载对话框，可以使用下面的接口，该接口直接通过 HTTP 流返回文件数据。

**URL**
```
GET http://localhost:8088/fms/ftp/download
```

**请求参数（Query 参数）**
- `filePath` (string, 必传)：文件在 FTP 上的相对路径，例如：`uploads/合格证/file.txt`

**请求说明**
- 接口流程：
    1. 根据提供的 `filePath` 拆分出远程文件夹和文件名；
    2. 调用 FTP 下载方法，将文件保存到本地临时目录；
    3. 将本地文件通过 `HttpServletResponse` 输出流传回给前端，从而实现直接下载；
    4. 下载完成后可清理本地临时文件。

**示例请求**
```
GET http://localhost:8088/fms/ftp/download?filePath=uploads/合格证/file.txt
```

**返回说明**
- 成功时，浏览器会弹出保存对话框或自动下载文件；
- 失败时，HTTP 状态码或响应体中会返回错误提示消息。

---

## 4. 文件删除接口()

**URL**
```
不开放
```

**请求参数（Query 参数）**
- `fileName` (string, 必传)：文件名标识（用于查找数据库中的文件记录，通常与 `documentTypeName` 关联，如 `合格证` 下的文件名）。
- `matchID` (number, 必传)：匹配 ID，用于定位数据库中的文件记录。

**请求说明**
- 接口会查找数据库中的文件记录，再获取对应文件夹信息，调用 FTP 删除方法删除对应远程文件，同时标记数据库记录为删除。

**示例请求**
```
DELETE http://localhost:8088/fms/ftp/delete?fileName=xxx_20250121155010.pdf&matchID=123
```

**示例响应（成功）**
```json
{
  "code": 200,
  "msg": "文件删除成功"
}
```

**示例响应（失败）**
```json
{
  "code": 500,
  "msg": "文件删除失败: 未找到对应的文件记录"
}
```

---
