package kongju.pickmeal.application.diet.data;

import java.util.Set;
import java.util.List;

import lombok.Builder;

import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.ai.AiDietGenerateDto;


@Builder
public record FamilyDietData(
        List<AiDietGenerateDto.Disease> diseases,
        List<AiDietGenerateDto.HealthCondition> healthConditions,

        List<Ingredient> preferredIngredients,

        Set<Long> allergyIngredientIds,
        Set<Long> fallbackExcludedIngredientIds,

        List<String> preferredIngredientNames,
        List<String> dislikedIngredientNames,
        List<String> allergyIngredientNames
) {
}