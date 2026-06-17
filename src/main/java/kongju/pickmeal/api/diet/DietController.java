package kongju.pickmeal.api.diet;

import jakarta.validation.Valid;
import kongju.pickmeal.api.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import kongju.pickmeal.application.diet.DietService;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.application.diet.data.MenuPickDto;


@RestController
@RequestMapping("/api/v1/diets")
@RequiredArgsConstructor
public class DietController {
    private final DietService dietService;

    @PostMapping("/menu-picks")
    @PreAuthorize("hasRole('LEADER') or hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<MenuPickDto.CreateResponse>> createMenuPick(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid MenuPickDto.CreateRequest request) {
        MenuPickDto.CreateResponse response = dietService.menuPick(userDetails, request);

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }
}
