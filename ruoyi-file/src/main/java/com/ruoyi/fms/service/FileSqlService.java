package com.ruoyi.fms.service;

import com.ruoyi.fms.mapper.FileSqlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FileSqlService {

    private static final Logger log = LoggerFactory.getLogger(FileSqlService.class);

    private final FileSqlMapper fileSqlMapper;

    public FileSqlService(FileSqlMapper fileSqlMapper) {
        this.fileSqlMapper = fileSqlMapper;
    }

    public List<Map<String, Object>> queryFilesByFileMtimeBetween(
            LocalDateTime startTime, LocalDateTime endTime) {

        log.debug("[filesql-service] 调用 Mapper 查询, startTime={}, endTime={}",
                startTime, endTime);

        List<Map<String, Object>> rows =
                fileSqlMapper.selectFilesByFileMtimeBetween(startTime, endTime);

        log.debug("[filesql-service] Mapper 查询完成, 返回记录数={}",
                (rows == null ? 0 : rows.size()));

        return rows;
    }
}
