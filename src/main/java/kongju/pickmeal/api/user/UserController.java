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
import kongju.pickmeal.application.user.data.UserHealthDto;
import kongju.pickmeal.application.user.data.UserProfileDto;
import kongju.pickmeal.application.user.data.UserDietProfileDto;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserDto.SignupResponse>> signup(@RequestBody @Valid UserDto.SignupRequest request) {
        UserDto.SignupResponse response = userService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PatchMapping("/me/diseases")
    public ResponseEntity<ApiResponse<Void>> updateDietProfile(
            @RequestBody @Valid UserDietProfileDto.UpdateDiseaseRequest request,
            @AuthenticationPrincipal User user) {

        userService.updateDisease(request, user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @PatchMapping("/me/ingredient-preferences")
    public ResponseEntity<ApiResponse<Void>> updateIngredientPreferences(
            @RequestBody @Valid UserDietProfileDto.UpdateIngredientPreferenceRequest request,
            @AuthenticationPrincipal User user) {

        userService.updateIngredientPreference(request, user);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @PatchMapping("/me/health")
    public ResponseEntity<ApiResponse<Void>> updateHealth(
            @RequestBody @Valid UserHealthDto.UpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        userService.updateHealth(request, user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileDto.UpdateResponse>> updateProfile(
            @RequestBody @Valid UserProfileDto.UpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        UserProfileDto.UpdateResponse response = userService.updateProfile(request, user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

}
