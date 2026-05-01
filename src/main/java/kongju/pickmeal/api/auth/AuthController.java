package kongju.pickmeal.api.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kongju.pickmeal.application.auth.AuthService;
import kongju.pickmeal.application.auth.data.request.AuthRequest;
import kongju.pickmeal.application.auth.data.response.AuthResponse;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse.Token>> login(@RequestBody @Valid AuthRequest.Login request, HttpServletResponse response){
        AuthResponse.Token token = authService.login(request, response);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(token));
    }

}
