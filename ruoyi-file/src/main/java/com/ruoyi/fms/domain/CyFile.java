package com.ruoyi.fms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 文件信息对象 cy_file
 * 
 * @author ruoyi
 * @date 2024-12-11
 */
public class CyFile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文件ID */
    private Long fileId;

    /** 所属文件夹ID */
    @Excel(name = "所属文件夹ID")
    private Long folderId;

    /** 文档类型（如：招标过程文档、投标过程文档等） */
    @Excel(name = "文档类型", readConverterExp = "如=：招标过程文档、投标过程文档等")
    private String documentType;

    /** 匹配ID，用于外部业务数据关联 */
    @Excel(name = "匹配ID，用于外部业务数据关联")
    private String matchId;

    /** 文件名称 */
    @Excel(name = "文件名称")
    private String documentName;

    /** 文件标签或关键字 */
    @Excel(name = "文件标签或关键字")
    private String documentLabel;

    /** 文件上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "文件上传时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date uploadTime;

    /** 文件信息修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "文件信息修改时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date modifyTime;

    /** 文件访问URL或存储路径 */
    @Excel(name = "文件访问URL或存储路径")
    private String url;

    /** 项目ID */
    @Excel(name = "项目ID")
    private String projectId;

    /** 销售合同号 */
    @Excel(name = "销售合同号")
    private String salesContractNo;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 送货单号 */
    @Excel(name = "送货单号")
    private String deliveryNoteNo;

    /** 批次号 */
    @Excel(name = "批次号")
    private String batchNo;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public void setFileId(Long fileId) 
    {
        this.fileId = fileId;
    }

    public Long getFileId() 
    {
        return fileId;
    }
    public void setFolderId(Long folderId) 
    {
        this.folderId = folderId;
    }

    public Long getFolderId() 
    {
        return folderId;
    }
    public void setDocumentType(String documentType) 
    {
        this.documentType = documentType;
    }

    public String getDocumentType() 
    {
        return documentType;
    }
    public void setMatchId(String matchId) 
    {
        this.matchId = matchId;
    }

    public String getMatchId() 
    {
        return matchId;
    }
    public void setDocumentName(String documentName) 
    {
        this.documentName = documentName;
    }

    public String getDocumentName() 
    {
        return documentName;
    }
    public void setDocumentLabel(String documentLabel) 
    {
        this.documentLabel = documentLabel;
    }

    public String getDocumentLabel() 
    {
        return documentLabel;
    }
    public void setUploadTime(Date uploadTime) 
    {
        this.uploadTime = uploadTime;
    }

    public Date getUploadTime() 
    {
        return uploadTime;
    }
    public void setModifyTime(Date modifyTime) 
    {
        this.modifyTime = modifyTime;
    }

    public Date getModifyTime() 
    {
        return modifyTime;
    }
    public void setUrl(String url) 
    {
        this.url = url;
    }

    public String getUrl() 
    {
        return url;
    }
    public void setProjectId(String projectId) 
    {
        this.projectId = projectId;
    }

    public String getProjectId() 
    {
        return projectId;
    }
    public void setSalesContractNo(String salesContractNo) 
    {
        this.salesContractNo = salesContractNo;
    }

    public String getSalesContractNo() 
    {
        return salesContractNo;
    }
    public void setMaterialCode(String materialCode) 
    {
        this.materialCode = materialCode;
    }

    public String getMaterialCode() 
    {
        return materialCode;
    }
    public void setDeliveryNoteNo(String deliveryNoteNo) 
    {
        this.deliveryNoteNo = deliveryNoteNo;
    }

    public String getDeliveryNoteNo() 
    {
        return deliveryNoteNo;
    }
    public void setBatchNo(String batchNo) 
    {
        this.batchNo = batchNo;
    }

    public String getBatchNo() 
    {
        return batchNo;
    }
    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("fileId", getFileId())
            .append("folderId", getFolderId())
            .append("documentType", getDocumentType())
            .append("matchId", getMatchId())
            .append("documentName", getDocumentName())
            .append("documentLabel", getDocumentLabel())
            .append("uploadTime", getUploadTime())
            .append("modifyTime", getModifyTime())
            .append("url", getUrl())
            .append("projectId", getProjectId())
            .append("salesContractNo", getSalesContractNo())
            .append("materialCode", getMaterialCode())
            .append("deliveryNoteNo", getDeliveryNoteNo())
            .append("batchNo", getBatchNo())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
