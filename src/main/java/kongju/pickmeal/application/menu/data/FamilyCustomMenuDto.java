package kongju.pickmeal.application.menu.data;

import java.util.List;
import java.math.BigDecimal;

import lombok.Builder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;
import kongju.pickmeal.core.menu.type.IngredientType;
import kongju.pickmeal.core.menu.type.IngredientUnit;


public class FamilyCustomMenuDto {
    @Builder
    public record SaveRequest(
            @NotBlank
            String menuName,
            @NotNull
            DishType dishType,
            @NotNull
            MenuCategory category,
            BigDecimal kcal,
            BigDecimal carbs,
            BigDecimal protein,
            BigDecimal fat,
            BigDecimal sodium,
            List<IngredientRequest> ingredients
    ) {
    }

    @Builder
    public record IngredientRequest(
            Long ingredientId,
            @NotBlank
            String ingredientName,
            @NotNull
            Double quantity,
            @NotNull
            IngredientUnit unit,
            @NotNull
            IngredientType type
    ) {
    }
}
