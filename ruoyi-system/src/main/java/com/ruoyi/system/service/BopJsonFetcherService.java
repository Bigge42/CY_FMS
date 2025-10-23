package com.ruoyi.system.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface BopJsonFetcherService {
    JsonNode fetchJsonByCode(String code) throws Exception;
    JsonNode errorJson(String msg);
}
