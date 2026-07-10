package kongju.pickmeal.api.menu;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;
import kongju.pickmeal.application.menu.MenuService;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.api.security.CustomUserDetails;
import kongju.pickmeal.application.menu.data.MenuDto.DetailResponse;
import kongju.pickmeal.application.menu.data.MenuDto.ListItemResponse;
import kongju.pickmeal.application.menu.data.MenuFilterDto.MetadataResponse;
import kongju.pickmeal.application.menu.data.FamilyCustomMenuDto.CreateRequest;


@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @GetMapping("/filter-options")
    public ResponseEntity<ApiResponse<MetadataResponse>> getMenuFilters() {
        MetadataResponse response = menuService.getFilterMetadata();

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ListItemResponse>> getMenuItems(
            @RequestParam(required = false) MenuCategory category,
            @RequestParam(required = false) DishType dishType,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        ListItemResponse response = menuService.searchMenus(category, dishType, keyword, pageable);

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<DetailResponse>> getMenuDetail(
            @PathVariable Long menuId
    ) {
        DetailResponse response = menuService.detailMenu(menuId);

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    @PostMapping("/custom")
    @PreAuthorize("hasRole('LEADER')")
    public ResponseEntity<ApiResponse<Void>> createCustomMenu(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateRequest request
    ) {
        menuService.createMenu(userDetails.id(), request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
