package kongju.pickmeal.application.diet.data;

import java.util.List;
import java.math.BigDecimal;

import kongju.pickmeal.core.menu.type.IngredientUnit;
import lombok.Builder;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.type.DishType;


public class DietMenuDto {
    @Builder
    public record ReplaceRequest(
            Long menuId
    ) {
    }

    @Builder
    public record ReplaceResponse(
            Long replacedMenuId,
            String menuName
    ) {
    }

    @Builder
    public record ReplacementMenuListResponse(
            Long dietId,
            String keyword,
            DishType dishType,
            List<ReplacementMenuResponse> menus,
            PageInfoResponse pageInfo
    ) {
    }

    @Builder
    public record ReplacementMenuResponse(
            Long menuId,
            String menuName,
            BigDecimal kcal
    ) {
        public static ReplacementMenuResponse from(Menu menu) {
            return ReplacementMenuResponse.builder()
                    .menuId(menu.getId())
                    .menuName(menu.getMenuName())
                    .kcal(menu.getKcal())
                    .build();
        }
    }

    @Builder
    public record PageInfoResponse(
            Integer currentPage,
            Integer totalPages,
            Long totalElements
    ) {
    }

    @Builder
    public record MenuDetailsResponse(
            Long dietId,
            Long menuId,
            String menuName,
            DishType dishType,
            BigDecimal kcal,
            BigDecimal carbs,
            BigDecimal protein,
            BigDecimal fat,
            BigDecimal sodium,
            List<IngredientsResponse> requiredIngredients
    ) {
    }

    @Builder
    public record IngredientsResponse(
            Long ingredientId,
            String name,
            String quantityText
    ) {
    }
}
