package kongju.pickmeal.core.ai;

import java.util.List;
import java.time.LocalDate;

import lombok.Builder;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.diet.type.MealType;


public class AiDietGenerateDto {
    @Builder
    public record Command(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            List<MenuCandidate> menuCandidates,
            List<UserMenuPick> userMenuPicks,
            List<String> healthConditions,
            List<String> preferredIngredients,
            List<String> dislikedIngredients
    ) {
    }

    @Builder
    public record Result(
            LocalDate startDate,
            LocalDate endDate,
            String summary,
            List<DayPlan> days
    ) {
    }

    @Builder
    public record DayPlan(
            LocalDate date,
            List<MealPlan> meals
    ) {
    }

    @Builder
    public record MealPlan(
            MealType mealType,
            Long menuId,
            String reason
    ) {
    }


    @Builder
    public record MenuCandidate(
            Menu menu,
            List<String> ingredients
    ) {
    }
}
