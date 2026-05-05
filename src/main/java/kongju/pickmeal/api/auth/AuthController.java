package kongju.pickmeal.api.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import kongju.pickmeal.application.auth.AuthService;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.application.auth.data.request.AuthRequest;
import kongju.pickmeal.application.auth.data.response.AuthResponse;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    private static final long REFRESH_TOKEN_MAX_AGE = Duration.ofDays(14).toSeconds();
    private void saveCookie(HttpServletResponse hResponse, String refreshToken, Long expiresIn) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(expiresIn)
                .sameSite("Strict")
                .build();

        hResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse.AccessToken>> login(
            @RequestBody @Valid AuthRequest.Login request,
            HttpServletResponse hResponse
    ) {
        AuthResponse.Token tokenSet = authService.login(request);

        // 쿠키에 담기
        saveCookie(hResponse, tokenSet.refreshToken(), REFRESH_TOKEN_MAX_AGE);

        AuthResponse.AccessToken response = AuthResponse.AccessToken
                .builder()
                .accessToken(tokenSet.accessToken())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody @Valid AuthRequest.Logout request,
            HttpServletResponse hResponse
    ) {
        authService.logout(request);

        // 쿠키 정보에서 삭제
        saveCookie(hResponse, "", 0L);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse.AccessToken>> refresh(
            @RequestBody @Valid AuthRequest.RefreshToken request,
            @CookieValue(name = "refreshToken", required = false
            ) String oldRefreshToken,
            HttpServletResponse hResponse) {
        AuthResponse.Token tokenSet = authService.refresh(request, oldRefreshToken);

        // 새 토큰으로 쿠키 덮어쓰기
        saveCookie(hResponse, tokenSet.refreshToken(), REFRESH_TOKEN_MAX_AGE);

        AuthResponse.AccessToken response = AuthResponse.AccessToken
                .builder()
                .accessToken(tokenSet.accessToken())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

}
