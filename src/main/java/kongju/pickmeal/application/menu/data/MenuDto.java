package kongju.pickmeal.application.menu.data;

import java.util.List;
import java.math.BigDecimal;

import lombok.Builder;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;


public class MenuDto {
    @Builder
    public record ListItemResponse(
            List<MenuInfoResponse> content,
            PageInfoResponse pageInfo
    ){}

    @Builder
    public record MenuInfoResponse(
            Long menuId,
            String menuName,
            MenuCategory category,
            DishType dishType,
            BigDecimal kcal
    ){}

    @Builder
    public record PageInfoResponse(
            Integer currentPage,
            Integer totalPages,
            Long totalElements
    ){}

    @Builder
    public record DetailResponse(
            Long menuId,
            String menuName,
            MenuCategory category,
            DishType dishType,
            BigDecimal kcal,
            BigDecimal carbs,
            BigDecimal protein,
            BigDecimal fat,
            BigDecimal sodium,
            List<IngredientResponse> ingredients
    ){}

    @Builder
    public record IngredientResponse(
        String ingredientName,
        String quantityText
    ){}
}
