package kongju.pickmeal.api.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.application.user.UserService;
import kongju.pickmeal.application.user.data.UserDto;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.application.user.data.UserDietProfileDto;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserDto.SignupResponse>> signup(@RequestBody @Valid UserDto.SignupRequest request){
        UserDto.SignupResponse response = userService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PatchMapping("/me/diet-profile")
    public ResponseEntity<ApiResponse<Void>> updateDietProfile(
            @RequestBody @Valid UserDietProfileDto.UpdateRequest request,
            @AuthenticationPrincipal User user){

        userService.updateDietProfile(request, user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }
}
