package kongju.pickmeal.api.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import kongju.pickmeal.application.user.data.*;
import kongju.pickmeal.application.user.UserService;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.api.security.CustomUserDetails;
import kongju.pickmeal.application.user.data.UserDto.WithdrawRequest;
import kongju.pickmeal.application.user.data.UserDietProfileDto.UpdateDiseaseRequest;
import kongju.pickmeal.application.user.data.UserDietProfileDto.UpdateIngredientPreferenceRequest;

import static kongju.pickmeal.application.user.data.UserDto.*;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @SecurityRequirements
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@RequestBody @Valid SignupRequest request) {
        SignupResponse response = userService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PatchMapping("/me/diseases")
    public ResponseEntity<ApiResponse<Void>> updateDietProfile(
            @RequestBody @Valid UpdateDiseaseRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        userService.updateDisease(request, userDetails.id());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @PatchMapping("/me/ingredient-preferences")
    public ResponseEntity<ApiResponse<Void>> updateIngredientPreferences(
            @RequestBody @Valid UpdateIngredientPreferenceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        userService.updateIngredientPreference(request, userDetails.id());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @PatchMapping("/me/health")
    public ResponseEntity<ApiResponse<Void>> updateHealth(
            @RequestBody @Valid UserHealthDto.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.updateHealth(request, userDetails.id());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileDto.UpdateResponse>> updateProfile(
            @RequestBody @Valid UserProfileDto.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserProfileDto.UpdateResponse response = userService.updateProfile(request, userDetails.id());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @RequestBody @Valid UserPasswordDto.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.updatePassword(request, userDetails.id());
        return ResponseEntity
                .ok(ApiResponse.success());
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid WithdrawRequest request) {
        userService.deleteUser(userDetails.id(), request);
        return ResponseEntity
                .ok(ApiResponse.success());
    }
}
