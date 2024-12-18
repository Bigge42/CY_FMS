package com.ruoyi.fms.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.fms.domain.CyFolder;
import com.ruoyi.fms.service.ICyFolderService;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 文件夹Controller
 * 
 * @author ruoyi
 * @date 2024-12-11
 */
@RestController
@RequestMapping("/fms/cyfolder")
public class CyFolderController extends BaseController
{
    @Autowired
    private ICyFolderService cyFolderService;

    /**
     * 查询文件夹列表
     */
    @PreAuthorize("@ss.hasPermi('fms:cyfolder:list')")
    @GetMapping("/list")
    public AjaxResult list(CyFolder cyFolder)
    {
        List<CyFolder> list = cyFolderService.selectCyFolderList(cyFolder);
        return success(list);
    }

    /**
     * 导出文件夹列表
     */
    @PreAuthorize("@ss.hasPermi('fms:cyfolder:export')")
    @Log(title = "文件夹", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CyFolder cyFolder)
    {
        List<CyFolder> list = cyFolderService.selectCyFolderList(cyFolder);
        ExcelUtil<CyFolder> util = new ExcelUtil<CyFolder>(CyFolder.class);
        util.exportExcel(response, list, "文件夹数据");
    }

    /**
     * 获取文件夹详细信息
     */
    @PreAuthorize("@ss.hasPermi('fms:cyfolder:query')")
    @GetMapping(value = "/{folderId}")
    public AjaxResult getInfo(@PathVariable("folderId") Long folderId)
    {
        return success(cyFolderService.selectCyFolderByFolderId(folderId));
    }

    /**
     * 新增文件夹
     */
    @PreAuthorize("@ss.hasPermi('fms:cyfolder:add')")
    @Log(title = "文件夹", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CyFolder cyFolder)
    {
        return toAjax(cyFolderService.insertCyFolder(cyFolder));
    }

    /**
     * 修改文件夹
     */
    @PreAuthorize("@ss.hasPermi('fms:cyfolder:edit')")
    @Log(title = "文件夹", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CyFolder cyFolder)
    {
        return toAjax(cyFolderService.updateCyFolder(cyFolder));
    }

    /**
     * 删除文件夹
     */
    @PreAuthorize("@ss.hasPermi('fms:cyfolder:remove')")
    @Log(title = "文件夹", businessType = BusinessType.DELETE)
	@DeleteMapping("/{folderIds}")
    public AjaxResult remove(@PathVariable Long[] folderIds)
    {
        return toAjax(cyFolderService.deleteCyFolderByFolderIds(folderIds));
    }
}
