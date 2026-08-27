package kongju.pickmeal.api.auth;

import java.time.Duration;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import kongju.pickmeal.application.auth.AuthService;
import kongju.pickmeal.application.auth.data.AuthDto;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.infrastructure.config.properties.CookieProperties;


@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieProperties cookieProperties;

    private static final long REFRESH_TOKEN_MAX_AGE = Duration.ofDays(14).toSeconds();

    private void saveCookie(HttpServletResponse hResponse, String refreshToken, Long expiresIn) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .path("/")
                .maxAge(expiresIn)
                .sameSite("Strict")
                .build();

        hResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.AccessTokenResponse>> login(
            @RequestBody @Valid AuthDto.LoginRequest request,
            HttpServletResponse hResponse
    ) {
        AuthDto.TokenPair tokenSet = authService.login(request);

        // 쿠키에 담기
        saveCookie(hResponse, tokenSet.refreshToken(), REFRESH_TOKEN_MAX_AGE);

        AuthDto.AccessTokenResponse response = AuthDto.AccessTokenResponse
                .builder()
                .accessToken(tokenSet.accessToken())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse hResponse
    ) {
        authService.logout(authorizationHeader, refreshToken);

        // 쿠키 정보에서 삭제
        deleteRefreshTokenCookie(hResponse);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(null));
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        saveCookie(response, "", 0L);
    }

    @SecurityRequirements
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDto.AccessTokenResponse>> refresh(
            @Parameter(hidden = true)
            @CookieValue(name = "refreshToken", required = false)
            String oldRefreshToken,
            HttpServletResponse hResponse) {

        AuthDto.TokenPair tokenSet = authService.refresh(oldRefreshToken);

        // 새 토큰으로 쿠키 덮어쓰기
        saveCookie(hResponse, tokenSet.refreshToken(), REFRESH_TOKEN_MAX_AGE);

        AuthDto.AccessTokenResponse response = AuthDto.AccessTokenResponse
                .builder()
                .accessToken(tokenSet.accessToken())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

}
