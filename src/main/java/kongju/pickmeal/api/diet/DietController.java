package kongju.pickmeal.api.diet;

import jakarta.validation.Valid;
import kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import kongju.pickmeal.application.diet.DietService;
import kongju.pickmeal.api.security.CustomUserDetails;
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
        MenuPickDto.CreateResponse response = dietService.menuPick(userDetails.id(), request);

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    @PatchMapping("/menu-picks/{pickId}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LEADER')")
    public ResponseEntity<ApiResponse<MenuPickDto.UpdateResponse>> updatePickItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long pickId,
            @RequestBody @Valid MenuPickDto.UpdateRequest request
    ) {
        MenuPickDto.UpdateResponse response = dietService.updatePickMenu(userDetails.id(), pickId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/menu-picks/{pickId}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LEADER')")
    public ResponseEntity<ApiResponse<MenuPickDto.DeleteResponse>>  deletePickItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long pickId
    ){
        MenuPickDto.DeleteResponse response = dietService.deletePickMenu(userDetails.id(), pickId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ai-generate")
    public ResponseEntity<ApiResponse<DietGenerationDto.GenerateResponse>> generate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid DietGenerationDto.GenerateRequest request
    ) {
        DietGenerationDto.GenerateResponse response = dietService.requestGeneration(userDetails.id(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
