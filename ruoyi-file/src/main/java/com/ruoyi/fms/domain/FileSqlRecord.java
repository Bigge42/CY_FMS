package com.ruoyi.fms.domain;

import java.time.LocalDateTime;

public class FileSqlRecord {

    private Long id;
    private String fileName;
    private LocalDateTime fileMtime;
    private LocalDateTime recordMtime;

    // --- getter/setter ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getFileMtime() {
        return fileMtime;
    }

    public void setFileMtime(LocalDateTime fileMtime) {
        this.fileMtime = fileMtime;
    }

    public LocalDateTime getRecordMtime() {
        return recordMtime;
    }

    public void setRecordMtime(LocalDateTime recordMtime) {
        this.recordMtime = recordMtime;
    }
}
