package kongju.pickmeal.api.menu;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.web.PageableDefault;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;
import kongju.pickmeal.application.menu.MenuService;
import kongju.pickmeal.application.menu.data.MenuDto;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.application.menu.data.MenuFilterDto;


@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @GetMapping("/filter-options")
    public ResponseEntity<ApiResponse<MenuFilterDto.MetadataResponse>> getMenuFilters() {
        MenuFilterDto.MetadataResponse response = menuService.getFilterMetadata();

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MenuDto.ListItemResponse>> getMenuItems(
            @RequestParam(required = false) MenuCategory category,
            @RequestParam(required = false) DishType dishType,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        MenuDto.ListItemResponse response = menuService.searchMenus(category, dishType, pageable);

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuDto.DetailResponse>> getMenuDetail(
            @PathVariable Long menuId
    ) {
        MenuDto.DetailResponse response = menuService.detailMenu(menuId);

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

}
