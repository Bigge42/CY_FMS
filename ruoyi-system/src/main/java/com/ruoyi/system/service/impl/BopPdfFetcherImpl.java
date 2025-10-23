package com.ruoyi.system.service.impl;

import com.ruoyi.system.service.BopPdfFetcherService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.ruoyi.common.cyutils.config.URLConstant.URL_GET_BOPPDF;

@Service
public class BopPdfFetcherImpl implements BopPdfFetcherService {

    @Override
    public File fetchPdf(String fnumber, String itemid, String revision) throws Exception {
        File baseDir = new File(URL_GET_BOPPDF);

        if (!baseDir.exists() || !baseDir.isDirectory()) {
            throw new RuntimeException("BOP 根目录不存在：" + URL_GET_BOPPDF);
        }

        List<File> fnumberDirs = Arrays.stream(baseDir.listFiles())
                .filter(f -> f.isDirectory() && f.getName().startsWith(fnumber + "&"))
                .collect(Collectors.toList());

        if (fnumberDirs.isEmpty()) {
            throw new RuntimeException("未找到物料文件夹：" + fnumber);
        }

        fnumberDirs.sort(Comparator.comparing(f -> f.getName().split("&")[1]));
        File latestFnumberDir = fnumberDirs.get(fnumberDirs.size() - 1);

        File targetDir = Arrays.stream(latestFnumberDir.listFiles())
                .filter(f -> f.isDirectory() && f.getName().equals(itemid + "&" + revision))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到对应工艺文件夹：" + itemid + "&" + revision));

        return Arrays.stream(targetDir.listFiles())
                .filter(f -> f.isFile() && f.getName().equals(itemid + "&" + revision + ".pdf"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到 PDF 文件：" + itemid + "&" + revision + ".pdf"));
    }
}
