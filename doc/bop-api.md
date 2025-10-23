# BOP 接口使用说明

本文档介绍了系统中用于读取 BOP（Bill of Process）资料的两个接口，包括请求参数、示例以及异常返回说明。接口默认部署在 `ruoyi-admin` 模块，对外提供 GET 请求。

> **注意**：接口依赖共享文件目录 `\\\\10.11.0.20\\tcfile\\BOP`，请确保部署环境能够访问该网络路径，且目录结构符合下述约定。

## 目录结构约定

BOP 根目录下按照物料号与版本组织内容，示例：

```
\\10.11.0.20\tcfile\BOP
└── 04023000341&A
    ├── 04023000341&0001
    │   ├── 04023000341&0001.json
    │   └── 04023000341&0001.pdf
    └── 04023000341&0002
        └── ...
```

- 第一级目录：`{物料编码}&{版本字母}`，按版本字母排序，接口会自动读取最新版本目录。
- 第二级目录：`{工序号}&{修订号}`。
- 文件命名：JSON 与 PDF 文件均为 `{工序号}&{修订号}.json|.pdf`。

## 1. 获取最新 BOP JSON

- **URL**：`GET /dataHtml/bop/json`
- **查询参数**：
  - `code` *(必填)*：物料编码，例如 `04023000341`。
- **成功返回**：HTTP 200，返回 JSON 内容。服务会自动选择最新版本的 JSON 文件，并从 `pdf` 列表元素中移除 `filepath` 字段。

示例响应：

```json
{
  "fnumber": "04023000341",
  "itemid": "04023000341",
  "revision": "0001",
  "pdf": [
    {
      "name": "工序卡片",
      "displayName": "04023000341&0001.pdf"
    }
  ]
}
```

- **失败返回**：HTTP 400，`{"error":"具体错误信息"}`。常见原因包括找不到对应目录或 JSON 文件。

## 2. 在线预览 BOP PDF

- **URL**：`GET /dataHtml/bop/pdf`
- **查询参数**：
  - `fnumber` *(必填)*：物料编码。
  - `itemid` *(必填)*：工序号。
  - `revision` *(必填)*：修订号。
- **成功返回**：HTTP 200，返回 `application/pdf` 流，浏览器默认在线预览。
- **失败返回**：HTTP 400，`{"error":"具体错误信息"}`。例如缺少 PDF 文件或目录不存在。

## 3. 错误排查建议

1. **确认共享目录可访问**：确保部署服务器能够访问 `\\\\10.11.0.20\\tcfile\\BOP`，并具有读取权限。
2. **核对目录命名**：目录及文件名必须完全匹配 `{值}&{版本/修订}` 结构，避免出现全角字符或额外空格。
3. **检查 JSON 内容**：若 JSON 结构异常或缺少 `pdf` 数组，接口将按原样返回解析后的结果。

如需进一步扩展接口功能，可参考以下类：

- `com.ruoyi.web.controller.tjffiles.BopController`
- `com.ruoyi.web.controller.tjffiles.BopPdfController`
- `com.ruoyi.system.service.impl.BopJsonFetcherImpl`
- `com.ruoyi.system.service.impl.BopPdfFetcherImpl`

