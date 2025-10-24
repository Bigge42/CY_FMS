package com.ruoyi.system.service;

import java.io.File;

public interface BopPdfFetcherService {
    File fetchPdf(String fnumber, String itemid, String revision) throws Exception;
}
