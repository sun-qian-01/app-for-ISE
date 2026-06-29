package com.ise.platform.modules.auth;

import com.ise.platform.common.api.ApiResponse;
import com.ise.platform.common.security.AuthContext;
import com.ise.platform.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthDto.LoginData> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        return ApiResponse.success(authService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/register")
    public ApiResponse<AuthDto.RegisterStudentResponse> register(@Valid @RequestBody AuthDto.RegisterStudentRequest request) {
        return ApiResponse.success(authService.registerStudent(request));
    }

    @GetMapping("/me")
    public ApiResponse<AuthDto.UserView> me() {
        CurrentUser user = AuthContext.requireUser();
        return ApiResponse.success(authService.me(user));
    }

    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody AuthDto.ChangePasswordRequest request) {
        CurrentUser user = AuthContext.requireUser();
        authService.changePassword(user, request.getOldPassword(), request.getNewPassword());
        return ApiResponse.success(null);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authService.logout(authorization.substring("Bearer ".length()).trim());
        }
        return ApiResponse.success(null);
    }
}
