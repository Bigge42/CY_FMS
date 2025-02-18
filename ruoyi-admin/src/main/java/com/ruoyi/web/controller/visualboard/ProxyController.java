package com.ruoyi.web.controller.visualboard;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.http.HttpUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dev-api")
public class ProxyController {

    @Anonymous // 允许匿名访问
    @PostMapping("/gateway/QFCJDP")
    public AjaxResult proxyToQFCJDP() {
        return processRequest("http://10.11.0.101:8003/gateway/QFCJDP");
    }

    @Anonymous // 允许匿名访问
    @PostMapping("/gateway/ZZCJCJDP")
    public AjaxResult proxyToZZCJCJDP() {
        return processRequest("http://10.11.0.101:8003/gateway/ZZCJCJDP");
    }

    @Anonymous
    @PostMapping("/gateway/XZFFJZPJY")
    public AjaxResult proxyToXZFFJZPJY() {
        return processRequest("http://10.11.0.101:8003/gateway/XZFFJZPJY");
    }

    @Anonymous
    @PostMapping("/gateway/ZTFFJZPJY")
    public AjaxResult proxyToZTFFJZPJY() {
        return processRequest("http://10.11.0.101:8003/gateway/ZTFFJZPJY");
    }

    @Anonymous
    @PostMapping("/gateway/XZFZPYS")
    public AjaxResult proxyToXZFZPYS() {
        return processRequest("http://10.11.0.101:8003/gateway/XZFZPYS");
    }


    @Anonymous
    @GetMapping("/gateway/mpkkb")
    public Object proxyToMPKKB() {
        try {
            String url = "http://10.11.0.101:8003/gateway/mpkkb";
            String result = HttpUtils.sendGet(url);

            // 检查返回的数据是否是一个 JSON 数组字符串
            if (result.startsWith("[") && result.endsWith("]")) {
                // 直接解析为 JSON 数组并返回
                return JSON.parseArray(result);
            }

            // 如果返回的是 JSON 对象，尝试提取其中的 "data" 字段
            JSONObject jsonData = JSON.parseObject(result);
            if (jsonData.containsKey("data")) {
                return jsonData.getJSONArray("data");
            }

            // 如果没有 "data" 字段，返回原始 JSON 数据
            return jsonData;
        } catch (Exception e) {
            e.printStackTrace();
            return "请求接口时发生错误：" + e.getMessage();
        }
    }
    @Anonymous
    @GetMapping("/gateway/ltkkb")
    public Object proxyToLTKKB() {
        try {
            String url = "http://10.11.0.101:8003/gateway/ltkkb";
            String result = HttpUtils.sendGet(url);

            // 检查返回的数据是否是一个 JSON 数组字符串
            if (result.startsWith("[") && result.endsWith("]")) {
                // 直接解析为 JSON 数组并返回
                return JSON.parseArray(result);
            }

            // 如果返回的是 JSON 对象，尝试提取其中的 "data" 字段
            JSONObject jsonData = JSON.parseObject(result);
            if (jsonData.containsKey("data")) {
                return jsonData.getJSONArray("data");
            }

            // 如果没有 "data" 字段，返回原始 JSON 数据
            return jsonData;
        } catch (Exception e) {
            e.printStackTrace();
            return "请求接口时发生错误：" + e.getMessage();
        }
    }



    /**
     * 通用处理请求的方法，过滤异常状态的数据并返回
     * @param targetUrl 目标接口URL
     * @return AjaxResult 包含过滤后的数据
     */
    private AjaxResult processRequest(String targetUrl) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String strData = HttpUtils.sendPost(targetUrl, "");
            JSONObject jsonData = JSON.parseObject(strData);

            if (jsonData == null) {
                return AjaxResult.error("返回的JSON数据为空");
            }

            // 如果GXMX不存在，合并JRKG和JRWG为GXMX
            if (!jsonData.containsKey("GXMX")) {
                if (jsonData.containsKey("JRKG") && jsonData.containsKey("JRWG")) {
                    // 获取JRKG和JRWG数据并合并为GXMX
                    JSONArray jrkgArray = jsonData.getJSONArray("JRKG");
                    JSONArray jrwgArray = jsonData.getJSONArray("JRWG");

                    // 合并数组
                    JSONArray gxmxArray = new JSONArray();
                    gxmxArray.addAll(jrkgArray);
                    gxmxArray.addAll(jrwgArray);

                    // 将合并后的数组放入jsonData中
                    jsonData.put("GXMX", gxmxArray);
                } else {
                    return AjaxResult.error("返回数据中缺少'JRKG'和'JRWG'，无法合并为'GXMX'");
                }
            }

            // 获取GXMX数组中abnormalState为异常的ERPPlanOrderId列表
            JSONArray gxmxArray = jsonData.getJSONArray("GXMX");
            List<String> abnormalERPPlanOrderIds = gxmxArray.stream()
                    .map(item -> (JSONObject) item)
                    .filter(item -> "异常".equals(item.getString("abnormalState")))
                    .map(item -> item.getString("ERPPlanOrderId"))
                    .collect(Collectors.toList());

            // 过滤ZPYCLB数组中FMTONO与abnormalERPPlanOrderIds相同的项
            if (!jsonData.containsKey("ZPYCLB")) {
                return AjaxResult.error("返回数据中缺少'ZPYCLB'键");
            }

            JSONArray zpyclbArray = jsonData.getJSONArray("ZPYCLB");
            List<JSONObject> filteredZPYCLB = zpyclbArray.stream()
                    .map(item -> (JSONObject) item)
                    .filter(item -> abnormalERPPlanOrderIds.contains(item.getString("FMTONO")))
                    .collect(Collectors.toList());

            // 更新jsonData中的ZPYCLB为过滤后的结果
            JSONArray updatedZPYCLB = new JSONArray(filteredZPYCLB);
            jsonData.put("ZPYCLB", updatedZPYCLB);

            // 直接返回更新后的jsonData
            return AjaxResult.success(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("请求目标接口时发生错误：" + e.getMessage());
        }
    }

}
