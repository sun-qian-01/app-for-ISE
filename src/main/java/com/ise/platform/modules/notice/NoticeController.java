package com.ise.platform.modules.notice;

import com.ise.platform.common.api.ApiResponse;
import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.security.AuthContext;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
