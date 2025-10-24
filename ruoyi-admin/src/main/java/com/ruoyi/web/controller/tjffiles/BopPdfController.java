package com.ruoyi.web.controller.tjffiles;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.system.service.BopPdfFetcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;

@RestController
@Anonymous
public class BopPdfController {

    @Autowired
    private BopPdfFetcherService bopPdfFetcherService;

    @GetMapping("/dataHtml/bop/pdf")
    public ResponseEntity<?> getPdf(
            @RequestParam String fnumber,
            @RequestParam String itemid,
            @RequestParam String revision
    ) {
        try {
            File pdfFile = bopPdfFetcherService.fetchPdf(fnumber, itemid, revision);

            InputStreamResource resource = new InputStreamResource(new FileInputStream(pdfFile));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + pdfFile.getName())
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfFile.length())
                    .body(resource);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"服务器内部错误\"}");
        }
    }
}
