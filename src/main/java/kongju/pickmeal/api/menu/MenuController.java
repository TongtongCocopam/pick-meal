package kongju.pickmeal.api.menu;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kongju.pickmeal.application.menu.MenuService;
import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.application.menu.data.MenuFilterDto;


@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @GetMapping("/filter-options")
    public ResponseEntity<ApiResponse<MenuFilterDto.MetadataResponse>> getMenuFilters(){
        MenuFilterDto.MetadataResponse response = menuService.getFilterMetadata();

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }
}
