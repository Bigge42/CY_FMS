package com.ruoyi.fms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface FileSqlMapper {

    /**
     * 按 file_mtime 时间区间查询文件
     * 返回列：file_name, file_mtime, record_mtime
     */
    List<Map<String, Object>> selectFilesByFileMtimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
