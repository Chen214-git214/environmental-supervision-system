package com.neusoft.supervision.feedback.controller;

import com.neusoft.supervision.common.ApiResponse;
import com.neusoft.supervision.feedback.dto.CityOptionResponse;
import com.neusoft.supervision.feedback.service.DictionaryService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 区域字典查询，对应接口文档《查询系统网格覆盖城市》。
 * 三种角色都需要（公众端选反馈城市、管理端筛选、网格端展示），因此拦截器对其放行已登录用户。
 */
@RestController
@RequestMapping("/api/grid-cities")
@CrossOrigin(originPatterns = {"http://localhost:517*", "http://127.0.0.1:517*"})
public class GridCityController {
    private final DictionaryService dictionaryService;

    public GridCityController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping
    public ApiResponse<List<CityOptionResponse>> list(@RequestParam(required = false) Long provinceId) {
        return ApiResponse.success(dictionaryService.cityOptions(provinceId));
    }
}
