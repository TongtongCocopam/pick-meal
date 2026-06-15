package kongju.pickmeal.application.menu.data;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;


public class MenuFilterDto {
    @Builder
    public record MetadataResponse(
            List<CategoryResponse> categories,
            List<DishTypeResponse> dishTypes
    ) {
    }

    @Builder
    public record CategoryResponse(
            MenuCategory name,
            String displayName
    ) {
    }

    @Builder
    public record DishTypeResponse(
            DishType name,
            String displayName
    ) {
    }

    @Builder
    public record ListItemResponse(
        List<MenuInfo> content,
        PageInfo pageInfo
    ){}

    @Builder
    public record MenuInfo(
        Long menuId,
        String menuName,
        MenuCategory category,
        DishType dishType,
        BigDecimal kcal
    ){}

    @Builder
    public record PageInfo(
        Integer currentPage,
        Integer totalPages,
        Long totalElements
    ){}
}