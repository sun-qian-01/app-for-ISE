package com.ise.platform.modules.kb;

import com.ise.platform.common.api.ApiResponse;
import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.error.BusinessException;
import com.ise.platform.common.error.ErrorCode;
import com.ise.platform.common.security.AuthContext;
import com.ise.platform.common.security.CurrentUser;
import com.ise.platform.modules.kb.rag.KbIndexerService;
import com.ise.platform.modules.kb.rag.CodexQaProperties;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/kb")
public class KbController {

    private final KbService kbService;
    private final KbIndexerService kbIndexerService;
    private final CodexQaProperties codexQaProperties;

    public KbController(KbService kbService, KbIndexerService kbIndexerService, CodexQaProperties codexQaProperties) {
        this.kbService = kbService;
        this.kbIndexerService = kbIndexerService;
        this.codexQaProperties = codexQaProperties;
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

    @PostMapping("/templates")
    public ApiResponse<KbDto.TemplateView> createTemplate(@Valid @RequestBody KbDto.CreateTemplateRequest request) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(kbService.createTemplate(user, request));
    }

    @PostMapping("/qa")
    public ApiResponse<KbDto.QaResponse> qa(@Valid @RequestBody KbDto.QaRequest request) {
        AuthContext.requireUser();
        return ApiResponse.success(kbService.qa(request));
    }

    @PostMapping(value = "/qa/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter qaStream(@Valid @RequestBody KbDto.QaRequest request) {
        AuthContext.requireUser();
        SseEmitter emitter = new SseEmitter(Math.max(240_000L, codexQaProperties.getTimeoutMs() + 60_000L));
        CompletableFuture.runAsync(() -> {
            try {
                emitter.send(SseEmitter.event().name("route").data(Map.of(
                    "provider", "codex",
                    "model", codexQaProperties.getModel(),
                    "mode", "primary",
                    "workdir", codexQaProperties.getWorkingDirectory(),
                    "fallback", "responses-api"
                )));
                emitter.send(SseEmitter.event().name("status").data(Map.of(
                    "message", "正在检索知识库并整理答案。"
                )));
                KbDto.QaResponse response = kbService.qa(request);
                emitter.send(SseEmitter.event().name("answer").data(response));
                emitter.send(SseEmitter.event().name("complete").data(Map.of("ok", true)));
                emitter.complete();
            } catch (Exception ex) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(Map.of(
                        "message", ex.getMessage() == null ? "问答流式请求失败" : ex.getMessage()
                    )));
                } catch (Exception ignored) {
                    // client may have already disconnected
                }
                emitter.completeWithError(ex);
            }
        });
        return emitter;
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
