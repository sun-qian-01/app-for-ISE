package com.ise.platform.modules.dict;

import com.ise.platform.common.api.ApiResponse;
import com.ise.platform.common.security.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dicts")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping
    public ApiResponse<Map<String, List<Map<String, String>>>> dicts(@RequestParam(required = false) String types) {
        AuthContext.requireUser();
        return ApiResponse.success(dictService.getDicts(types));
    }
}
