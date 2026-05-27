package com.ise.platform.modules.honor;

import com.ise.platform.common.api.ApiResponse;
import com.ise.platform.common.security.AuthContext;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/honors")
public class HonorController {

    private final HonorService honorService;

    public HonorController(HonorService honorService) {
        this.honorService = honorService;
    }

    @GetMapping("/my")
    public ApiResponse<List<HonorDto.HonorView>> myHonors() {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(honorService.myHonors(user));
    }

    @GetMapping
    public ApiResponse<List<HonorDto.HonorView>> allHonors() {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(honorService.allHonors(user));
    }
}
