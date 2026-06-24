package kongju.pickmeal.core.ai;

import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;

import lombok.Builder;

import kongju.pickmeal.core.user.type.Gender;
import kongju.pickmeal.core.diet.type.MealType;
import kongju.pickmeal.core.menu.type.DishType;


public class AiDietGenerateDto {
    @Builder
    public record Command(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            List<MenuCandidate> menuCandidates,
            List<UserMenu> userMenuPicks,

            List<HealthCondition> healthConditions,
            List<Disease> disease,

            List<String> preferredIngredients,
            List<String> dislikedIngredients,
            List<String> allergyIngredients

    ) {
    }

    @Builder
    public record Disease(
            // 질병 이름 상세설명
            String diseaseName,
            String description
    ){}


    @Builder
    public record HealthCondition(
            // 성별, 연령, 키, 몸무게
            Gender gender,
            BigDecimal weight,
            BigDecimal height,
            int age
    ){}

    @Builder
    public record MenuCandidate(
            Long menuId,
            String menuName,
            DishType dishType,
            List<String> ingredients
    ) {
    }

    @Builder
    public record UserMenu(
            Long userMenuPickId,
            Long menuId,
            String menuName,
            DishType dishType,
            List<String> ingredients
    ) {
    }

    @Builder
    public record Result(
            LocalDate startDate,
            LocalDate endDate,
            List<MealPlan> mealPlans
    ) {
    }

    @Builder
    public record MealPlan(
            LocalDate date,
            MealType mealType,
            Long menuId
    ) {
    }
}
