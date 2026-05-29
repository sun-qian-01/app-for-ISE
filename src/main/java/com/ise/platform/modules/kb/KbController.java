package com.ise.platform.modules.kb;

import com.ise.platform.common.api.ApiResponse;
import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.AuthContext;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.modules.kb.rag.KbIndexerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kb")
public class KbController {

    private final KbService kbService;
    private final KbIndexerService kbIndexerService;

    public KbController(KbService kbService, KbIndexerService kbIndexerService) {
        this.kbService = kbService;
        this.kbIndexerService = kbIndexerService;
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

    @GetMapping("/articles/{articleId}")
    public ApiResponse<KbDto.ArticleDetailView> articleDetail(@PathVariable Long articleId) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(kbService.articleDetail(user, articleId));
    }

    @GetMapping("/templates")
    public ApiResponse<List<KbDto.TemplateView>> templates() {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(kbService.templates(user));
    }

    @PostMapping("/qa")
    public ApiResponse<KbDto.QaResponse> qa(@Valid @RequestBody KbDto.QaRequest request) {
        AuthContext.requireUser();
        return ApiResponse.success(kbService.qa(request));
    }

    @PostMapping("/rag/reindex")
    public ApiResponse<KbDto.RagReindexResponse> reindex() {
        CurrentUser user = AuthContext.requireUser();
        if (user.getRoles().stream().noneMatch(role ->
            "teacher_admin".equals(role) || "college_leader".equals(role) || "system_admin".equals(role))) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "permission denied");
        }
        int indexedChunks = kbIndexerService.reindexPublishedArticles();
        return ApiResponse.success(new KbDto.RagReindexResponse(indexedChunks));
    }
}
