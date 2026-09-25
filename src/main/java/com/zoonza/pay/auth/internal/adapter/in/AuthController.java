package com.zoonza.pay.auth.internal.adapter.in;

import com.zoonza.pay.auth.internal.adapter.in.dto.LoginRequest;
import com.zoonza.pay.auth.internal.adapter.in.dto.LoginResponse;
import com.zoonza.pay.auth.internal.adapter.in.support.TokenCookieManager;
import com.zoonza.pay.auth.internal.application.dto.LoginResult;
import com.zoonza.pay.auth.internal.application.port.in.AuthCommandUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final TokenCookieManager tokenCookieManager;
    private final AuthCommandUseCase authCommandUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResult result = authCommandUseCase.login(request.toCommand());
        ResponseCookie cookie = tokenCookieManager.refreshTokenCookie(result.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(result.accessToken().value()));
    }
}
