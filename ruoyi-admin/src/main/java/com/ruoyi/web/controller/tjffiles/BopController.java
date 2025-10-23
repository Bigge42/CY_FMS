package com.ruoyi.web.controller.tjffiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruoyi.system.service.BopJsonFetcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BopController {

    @Autowired
    private BopJsonFetcherService bopJsonFetcherService;

    /**
     * GET 接口：获取 BOP JSON
     * 示例访问：http://localhost:8080/dataHtml/bop/json?code=04023000341
     *
     * @param code 编码
     * @return 去掉 filepath 的 JSON
     */
    @GetMapping("/dataHtml/bop/json")
    public JsonNode getBopJson(@RequestParam String code) {
        try {
            return bopJsonFetcherService.fetchJsonByCode(code);
        } catch (Exception e) {
            e.printStackTrace();
            return bopJsonFetcherService.errorJson(e.getMessage());
        }
    }
}
