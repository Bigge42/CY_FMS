package com.ruoyi.fms.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.TreeEntity;

/**
 * 文件夹对象 cy_folder
 * 
 * @author ruoyi
 * @date 2024-12-11
 */
public class CyFolder extends TreeEntity
{
    private static final long serialVersionUID = 1L;

    /** 文件夹ID */
    private Long folderId;

    /** 文件夹名称（如：图纸PDF, 工艺文件, SMT文件, ...） */
    @Excel(name = "文件夹名称", readConverterExp = "如=：图纸PDF,,工=艺文件,,S=MT文件,,.=..")
    private String folderName;

    /** 文件夹代号（如：tcfile、TZ、smtfile、zbwj） */
    @Excel(name = "文件夹代号", readConverterExp = "如=：tcfile、TZ、smtfile、zbwj")
    private String folderCode;

    /** 对应物理路径(如：E:\tcfile\TZ)，可选 */
    @Excel(name = "对应物理路径(如：E:\tcfile)，可选")
    private String absolutePath;

    /** 文件夹状态（0正常 1停用） */
    @Excel(name = "文件夹状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 文件夹用途描述 */
    @Excel(name = "文件夹用途描述")
    private String description;

    /** 负责人（可选） */
    @Excel(name = "负责人", readConverterExp = "可=选")
    private String leader;

    /** 联系电话（可选） */
    @Excel(name = "联系电话", readConverterExp = "可=选")
    private String phone;

    /** 邮箱（可选） */
    @Excel(name = "邮箱", readConverterExp = "可=选")
    private String email;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public void setFolderId(Long folderId) 
    {
        this.folderId = folderId;
    }

    public Long getFolderId() 
    {
        return folderId;
    }
    public void setFolderName(String folderName) 
    {
        this.folderName = folderName;
    }

    public String getFolderName() 
    {
        return folderName;
    }
    public void setFolderCode(String folderCode) 
    {
        this.folderCode = folderCode;
    }

    public String getFolderCode() 
    {
        return folderCode;
    }
    public void setAbsolutePath(String absolutePath) 
    {
        this.absolutePath = absolutePath;
    }

    public String getAbsolutePath() 
    {
        return absolutePath;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }
    public void setLeader(String leader) 
    {
        this.leader = leader;
    }

    public String getLeader() 
    {
        return leader;
    }
    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }
    public void setEmail(String email) 
    {
        this.email = email;
    }

    public String getEmail() 
    {
        return email;
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
            .append("folderId", getFolderId())
            .append("parentId", getParentId())
            .append("ancestors", getAncestors())
            .append("folderName", getFolderName())
            .append("folderCode", getFolderCode())
            .append("absolutePath", getAbsolutePath())
            .append("orderNum", getOrderNum())
            .append("status", getStatus())
            .append("description", getDescription())
            .append("leader", getLeader())
            .append("phone", getPhone())
            .append("email", getEmail())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
