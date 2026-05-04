package kongju.pickmeal.api.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kongju.pickmeal.application.auth.AuthService;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.application.auth.data.request.AuthRequest;
import kongju.pickmeal.application.auth.data.response.AuthResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse.Token>> login(@RequestBody @Valid AuthRequest.Login request, HttpServletResponse response) {
        AuthResponse.Token token = authService.login(request, response);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody @Valid AuthRequest.Logout request, HttpServletResponse hResponse) {
        authService.logout(request, hResponse);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(null));
    }
}
