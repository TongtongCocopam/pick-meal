package kongju.pickmeal.application.diet.data;

import java.util.List;
import java.time.YearMonth;
import java.time.LocalDate;
import java.math.BigDecimal;

import lombok.Builder;

import kongju.pickmeal.core.diet.type.MealType;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.IngredientUnit;


public class DietDto {
    @Builder
    public record ListItemResponse(
            YearMonth month,
            int totalDays,
            List<DietResponse> diets,
            Boolean isGenerated
    ) {
    }

    @Builder
    public record DietResponse(
            LocalDate date,
            List<MealResponse> meals
    ) {
    }

    @Builder
    public record MealResponse(
            Long dietId,
            MealType mealType,
            DishType dishType,
            String menuName
    ) {
    }

    @Builder
    public record DailyDetailResponse(
            LocalDate date,
            BigDecimal totalCalories,
            BigDecimal totalCarbs,
            BigDecimal totalProtein,
            BigDecimal totalFat,
            BigDecimal totalSodium,
            List<DailyMealResponse> meals,
            List<IngredientsResponse> totalIngredients
    ) {
    }

    @Builder
    public record DailyMealResponse(
            MealType mealType,
            BigDecimal mealCalories,
            List<MenuItemResponse> meals
    ) {
    }

    @Builder
    public record MenuItemResponse(
            Long menuId,
            String menuName,
            DishType dishType,
            BigDecimal kcal,
            BigDecimal carbs,
            BigDecimal protein,
            BigDecimal fat,
            BigDecimal sodium,
            List<IngredientsResponse> requiredIngredients,
            boolean familyChoice
    ) {
    }

    @Builder
    public record IngredientsResponse(
            Long ingredientId,
            String name,
            Double quantity,
            IngredientUnit unit
    ) {
    }
}
