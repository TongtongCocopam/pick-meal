package kongju.pickmeal.api.user;

import jakarta.validation.Valid;
import kongju.pickmeal.application.user.UserService;
import kongju.pickmeal.application.user.data.MemberRequest;
import kongju.pickmeal.application.user.data.MemberResponse;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponse.Register>> signup(@RequestBody @Valid MemberRequest.Register request){
        MemberResponse.Register response = userService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
