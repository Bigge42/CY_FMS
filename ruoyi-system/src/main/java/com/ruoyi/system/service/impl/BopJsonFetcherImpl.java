package com.ruoyi.system.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.system.service.BopJsonFetcherService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.ruoyi.common.cyutils.config.URLConstant.URL_GET_BOPPDF;

@Service
public class BopJsonFetcherImpl implements BopJsonFetcherService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public JsonNode fetchJsonByCode(String code) throws Exception {
        File baseDir = new File(URL_GET_BOPPDF);

        if (!baseDir.exists() || !baseDir.isDirectory()) {
            throw new RuntimeException("BOP 根目录不存在：" + URL_GET_BOPPDF);
        }

        List<File> candidateDirs = Arrays.stream(baseDir.listFiles())
                .filter(f -> f.isDirectory() && f.getName().startsWith(code + "&"))
                .collect(Collectors.toList());

        if (candidateDirs.isEmpty()) {
            throw new RuntimeException("未找到文件夹：" + code);
        }

        candidateDirs.sort(Comparator.comparing(f -> f.getName().split("&")[1]));
        File latestDir = candidateDirs.get(candidateDirs.size() - 1);

        List<File> jsonFiles = Arrays.stream(latestDir.listFiles())
                .filter(f -> f.isFile() && f.getName().endsWith(".json"))
                .collect(Collectors.toList());

        if (jsonFiles.isEmpty()) {
            throw new RuntimeException("未找到 JSON 文件：" + latestDir.getAbsolutePath());
        }

        jsonFiles.sort(Comparator.comparing(f -> f.getName().split("&")[1]));
        File latestJson = jsonFiles.get(jsonFiles.size() - 1);

        JsonNode rootNode;
        try (FileInputStream fis = new FileInputStream(latestJson)) {
            rootNode = MAPPER.readTree(fis);
        }

        if (rootNode.has("pdf") && rootNode.get("pdf").isArray()) {
            ArrayNode pdfArray = (ArrayNode) rootNode.get("pdf");
            for (JsonNode node : pdfArray) {
                if (node instanceof ObjectNode) {
                    ((ObjectNode) node).remove("filepath");
                }
            }
        }

        return rootNode;
    }

    @Override
    public JsonNode errorJson(String message) {
        ObjectNode objectNode = MAPPER.createObjectNode();
        objectNode.put("error", message);
        return objectNode;
    }
}
