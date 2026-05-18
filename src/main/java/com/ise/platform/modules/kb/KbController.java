package com.ise.platform.modules.kb;

import com.ise.platform.common.api.ApiResponse;
import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.security.AuthContext;
import com.ise.platform.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/kb")
public class KbController {

    private final KbService kbService;

    public KbController(KbService kbService) {
        this.kbService = kbService;
    }

    @GetMapping("/articles")
    public ApiResponse<PagedData<KbDto.ArticleView>> listArticles(
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String publishStatus,
        @RequestParam(required = false) String tag
    ) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(kbService.listArticles(user, pageNo, pageSize, keyword, categoryId, publishStatus, tag));
    }

    @PostMapping("/qa")
    public ApiResponse<KbDto.QaResponse> qa(@Valid @RequestBody KbDto.QaRequest request) {
        AuthContext.requireUser();
        return ApiResponse.success(kbService.qa(request));
    }
}
