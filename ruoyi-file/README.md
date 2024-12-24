
---

## **一、文件上传功能**

### 1. **接口说明**
- **URL**：`http://localhost:8088/fms/ftp/upload`
- **方法**：`POST`
- **参数**：
    - `file`：通过 `form-data` 上传的文件。
- **响应**：
    - 成功时返回：`{"code": 200, "msg": "文件上传成功"}`
    - 失败时返回：`{"code": 500, "msg": "文件上传失败: <具体错误信息>"}`

---


### 2. **Postman 测试配置**

#### 配置步骤：
1. **Method**：选择 `POST`。
2. **URL**：输入接口地址：`http://localhost:8088/fms/ftp/upload`。
3. **Body**：
    - 选择 **form-data**。
    - 添加字段：
        - **Key**：`file`
        - **Value**：选择类型为 **File**，然后选择实际文件（如 `test.txt`）。
4. **发送请求**：
    - 点击 **Send**，观察响应。

#### 成功响应示例：
```json
{
    "code": 200,
    "msg": "文件上传成功"
}
```

---

## **二、文件下载功能**

### 1. **接口说明**
- **URL**：`http://localhost:8088/fms/ftp/download`
- **方法**：`GET`
- **参数**：
    - `fileName`：要下载的文件名。
- **响应**：
    - 成功时返回：文件内容作为 `application/octet-stream`。
    - 文件不存在时返回：`{"code": 404, "msg": "文件不存在: <文件名>"}`

---


### 2. **Postman 测试配置**

#### 配置步骤：
1. **Method**：选择 `GET`。
2. **URL**：输入接口地址：`http://localhost:8088/fms/ftp/download?fileName=test.txt`。
3. **Params**：
    - 添加参数：
        - **Key**：`fileName`
        - **Value**：`test.txt`
4. **发送请求**：
    - 点击 **Send**，观察响应。

#### 成功响应：
- 如果 Postman 显示为二进制流，可以点击右下角的 **Save Response** 按钮，保存文件到本地。


---

## **三、常见问题排查**

### 文件上传
1. **错误：`Current request is not a multipart request`**
    - 确认 Postman 的 Body 类型为 **form-data**，并选择文件类型。

2. **文件未保存到临时路径**
    - 检查路径 `C:/temp/` 是否存在，如果不存在会导致保存失败。
    - 后端日志中检查文件路径是否正确。

3. **上传文件到 FTP 失败**
    - 检查 FTP 服务器的配置是否正确。
    - 确认 FTP 的用户名和密码是否有写入权限。

---

### 文件下载
1. **错误：文件不存在**
    - 确认路径 `C:/temp/` 下是否存在指定的文件。
    - 如果文件已经上传到 FTP 服务器，但未同步到本地临时路径，检查 `ftpService.downloadFile` 方法是否正常工作。

2. **文件内容损坏**
    - 确保文件以 `application/octet-stream` 格式返回。
    - 验证文件内容是否正确读取和写入。

---

## **五、测试环境配置**

### 1. Spring Boot 配置
在 `application.yml` 中配置文件上传限制：
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

### 2. FTP 服务配置
- 确保 FTP 服务正常运行，并有正确的权限配置。
- 测试 FTP 上传和下载功能是否正常。

