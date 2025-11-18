package com.ruoyi.fms.controller;


import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.fms.service.FileSqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/filesql")
public class FileSqlController {

    private static final Logger log = LoggerFactory.getLogger(FileSqlController.class);

    private final FileSqlService fileSqlService;

    public FileSqlController(FileSqlService fileSqlService) {
        this.fileSqlService = fileSqlService;
    }

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * GET /filesql/list?start=2025-01-01%2000:00:00&end=2025-01-02%2000:00:00
     * 返回列：file_name, file_mtime, record_mtime
     */
    @Anonymous
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam("start") String start,
            @RequestParam("end") String end) {

        long t0 = System.currentTimeMillis();
        log.info("[filesql] 收到文件查询请求, start={}, end={}", start, end);

        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            startTime = LocalDateTime.parse(start, FORMATTER);
            endTime   = LocalDateTime.parse(end, FORMATTER);
        } catch (Exception e) {
            log.warn("[filesql] 时间解析失败, start={}, end={}, error={}",
                    start, end, e.getMessage(), e);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "时间格式错误，正确格式：yyyy-MM-dd HH:mm:ss");
            return err;
        }

        log.info("[filesql] 解析后的时间区间, startTime={}, endTime={}", startTime, endTime);

        // 调用 Service 查询
        List<Map<String, Object>> rows = fileSqlService.queryFilesByFileMtimeBetween(startTime, endTime);

        long t1 = System.currentTimeMillis();
        int size = (rows == null ? 0 : rows.size());
        log.info("[filesql] 查询结束, 记录数={}, 耗时={} ms", size, (t1 - t0));

        // 预览首尾几条日志（便于快速看数据是否正常）
        if (size > 0) {
            Map<String, Object> first = rows.get(0);
            log.debug("[filesql] 首条记录: file_name={}, file_mtime={}, record_mtime={}",
                    first.get("file_name"), first.get("file_mtime"), first.get("record_mtime"));

            if (size > 1) {
                Map<String, Object> last = rows.get(size - 1);
                log.debug("[filesql] 末条记录: file_name={}, file_mtime={}, record_mtime={}",
                        last.get("file_name"), last.get("file_mtime"), last.get("record_mtime"));
            }
        } else {
            log.info("[filesql] 本次时间区间内没有任何记录。");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", rows);
        result.put("count", size);
        result.put("start", startTime.format(FORMATTER));
        result.put("end", endTime.format(FORMATTER));
        return result;
    }
}
