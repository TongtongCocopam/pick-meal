package kongju.pickmeal.api.diet;

import java.time.LocalDate;
import java.time.YearMonth;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import kongju.pickmeal.application.diet.DietService;
import kongju.pickmeal.api.security.CustomUserDetails;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.application.diet.data.MenuPickDto;
import kongju.pickmeal.application.diet.data.DietMenuDto;
import kongju.pickmeal.application.diet.data.DietDto.ListItemResponse;
import kongju.pickmeal.application.diet.data.MenuPickDto.CreateResponse;
import kongju.pickmeal.application.diet.data.MenuPickDto.DeleteResponse;
import kongju.pickmeal.application.diet.data.MenuPickDto.UpdateResponse;
import kongju.pickmeal.application.diet.data.DietMenuDto.ReplaceResponse;
import kongju.pickmeal.application.diet.data.DietDto.DailyDetailResponse;
import kongju.pickmeal.application.diet.data.DietMenuDto.MenuDetailsResponse;
import kongju.pickmeal.application.diet.data.DietMenuDto.RecommendationResponse;
import kongju.pickmeal.application.diet.data.DietMenuDto.ReplacementMenuListResponse;

import static kongju.pickmeal.infrastructure.external.ai.data.DietGenerationDto.*;


@RestController
@RequestMapping("/api/v1/diets")
@RequiredArgsConstructor
public class DietController {
    private final DietService dietService;

    @PostMapping("/menu-picks")
    @PreAuthorize("hasRole('LEADER') or hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<CreateResponse>> createMenuPick(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid MenuPickDto.CreateRequest request) {
        CreateResponse response = dietService.menuPick(userDetails.id(), request);

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    @PatchMapping("/menu-picks/{pickId}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LEADER')")
    public ResponseEntity<ApiResponse<UpdateResponse>> updatePickItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long pickId,
            @RequestBody @Valid MenuPickDto.UpdateRequest request
    ) {
        UpdateResponse response = dietService.updatePickMenu(userDetails.id(), pickId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/menu-picks/{pickId}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LEADER')")
    public ResponseEntity<ApiResponse<DeleteResponse>> deletePickItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long pickId
    ) {
        DeleteResponse response = dietService.deletePickMenu(userDetails.id(), pickId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ai-generate")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<GenerateResponse>> generate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid GenerateRequest request
    ) {
        GenerateResponse response = dietService.requestGeneration(userDetails.id(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('LEADER')")
    public ResponseEntity<ApiResponse<ListItemResponse>> getAllDiets(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        ListItemResponse response = dietService.getDiets(userDetails.id(), month);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{date}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('LEADER')")
    public ResponseEntity<ApiResponse<DailyDetailResponse>> getDailyDiets(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable LocalDate date
    ) {
        DailyDetailResponse response = dietService.getDailyMeals(userDetails.id(), date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{dietId}/menu")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<ReplaceResponse>> updateMenu(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long dietId,
            @RequestBody @Valid DietMenuDto.ReplaceRequest request
    ) {
        ReplaceResponse response = dietService.replaceMenu(userDetails.id(), dietId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{dietId}/replacement-menus")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<ReplacementMenuListResponse>> getReplacementMenus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long dietId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        ReplacementMenuListResponse response = dietService.replacementMenus(userDetails.id(), dietId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{dietId}/replacement-menus/{menuId}")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<MenuDetailsResponse>> getReplacementMenuDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long dietId,
            @PathVariable Long menuId
    ) {
        MenuDetailsResponse response = dietService.menuDetails(userDetails.id(), dietId, menuId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{dietId}/replacement-menu-suggestions")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<RecommendationResponse>> replacementMenuSuggestions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long dietId
    ) {
        RecommendationResponse response = dietService.recommendations(userDetails.id(), dietId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
