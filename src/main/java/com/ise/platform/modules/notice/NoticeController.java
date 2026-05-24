package com.ise.platform.modules.notice;

import com.ise.platform.common.api.ApiResponse;
import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.security.AuthContext;
import com.ise.platform.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/my")
    public ApiResponse<PagedData<NoticeDto.NoticeView>> myNotices(
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String readStatus,
        @RequestParam(required = false) String tag
    ) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(noticeService.myNotices(user, pageNo, pageSize, readStatus, tag));
    }

    @GetMapping("/my/{noticeId}")
    public ApiResponse<NoticeDto.NoticeView> myNoticeDetail(@PathVariable Long noticeId) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(noticeService.myNoticeDetail(user, noticeId));
    }

    @GetMapping
    public ApiResponse<PagedData<NoticeDto.NoticeView>> notices(
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String readStatus,
        @RequestParam(required = false) String keyword
    ) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(noticeService.listNotices(user, pageNo, pageSize, readStatus, keyword));
    }

    @PostMapping
    public ApiResponse<NoticeDto.NoticeView> createNotice(@Valid @RequestBody NoticeDto.CreateNoticeRequest request) {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(noticeService.createNotice(user, request));
    }

    @PostMapping("/{noticeId}/read")
    public ApiResponse<Void> markRead(@PathVariable Long noticeId) {
        CurrentUser user = AuthContext.requireUser();
        noticeService.markRead(user, noticeId);
        return ApiResponse.success(null);
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        CurrentUser user = AuthContext.requireUser();
        noticeService.markAllRead(user);
        return ApiResponse.success(null);
    }
}
