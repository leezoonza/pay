package com.zoonza.pay.auth.internal.adapter.in;

import com.zoonza.pay.auth.internal.adapter.in.dto.LoginRequest;
import com.zoonza.pay.auth.internal.adapter.in.dto.TokenResponse;
import com.zoonza.pay.auth.internal.adapter.in.support.TokenCookieManager;
import com.zoonza.pay.auth.internal.application.dto.TokenResult;
import com.zoonza.pay.auth.internal.application.port.in.AuthCommandUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final TokenCookieManager tokenCookieManager;
    private final AuthCommandUseCase authCommandUseCase;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenResult result = authCommandUseCase.login(request.toCommand());
        ResponseCookie cookie = tokenCookieManager.createRefreshTokenCookie(result.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponse(result.accessToken().value()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(
            @CookieValue(name = TokenCookieManager.REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        TokenResult result = authCommandUseCase.reissue(refreshToken);
        ResponseCookie cookie = tokenCookieManager.createRefreshTokenCookie(result.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponse(result.accessToken().value()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = TokenCookieManager.REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        authCommandUseCase.logout(refreshToken);
        ResponseCookie cookie = tokenCookieManager.expiredRefreshTokenCookie();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
