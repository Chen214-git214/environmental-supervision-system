package com.neusoft.supervision.feedback.controller;

import com.neusoft.supervision.common.ApiResponse;
import com.neusoft.supervision.feedback.dto.AqiLevelResponse;
import com.neusoft.supervision.feedback.service.DictionaryService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AQI 级别字典查询，供前端渲染级别下拉，避免前端再硬编码一份级别列表。
 */
@RestController
@RequestMapping("/api/aqi-levels")
@CrossOrigin(originPatterns = {"http://localhost:517*", "http://127.0.0.1:517*"})
public class AqiLevelController {
    private final DictionaryService dictionaryService;

    public AqiLevelController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping
    public ApiResponse<List<AqiLevelResponse>> list() {
        return ApiResponse.success(dictionaryService.aqiLevels());
    }
}
