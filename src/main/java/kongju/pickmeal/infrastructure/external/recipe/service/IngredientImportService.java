package kongju.pickmeal.infrastructure.external.recipe.service;


import kongju.pickmeal.infrastructure.external.recipe.RecipeApiClient;
import kongju.pickmeal.infrastructure.external.recipe.mapper.IngredientMapper;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.core.diet.Ingredient;
import kongju.pickmeal.core.diet.MenuIngredient;
import kongju.pickmeal.core.diet.repository.MenuRepository;
import kongju.pickmeal.core.diet.repository.IngredientRepository;
import kongju.pickmeal.core.diet.repository.MenuIngredientRepository;
import kongju.pickmeal.infrastructure.external.recipe.data.ingredient.RecipeIngredientRow;
import kongju.pickmeal.infrastructure.external.recipe.data.ingredient.RecipeIngredientApiResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class IngredientImportService {

    private final MenuRepository menuRepository;
    private final RecipeApiClient recipeApiClient;
    private final IngredientMapper ingredientMapper;
    private final IngredientRepository ingredientRepository;
    private final MenuIngredientRepository menuIngredientRepository;

    public void importIngredients(int startIdx, int endIdx) {
        RecipeIngredientApiResponse response =
                recipeApiClient.fetchRecipeIngredients(startIdx, endIdx);

        for (RecipeIngredientRow row : response.grid().row()) {
            Menu menu = menuRepository.findByExternalRecipeId(row.recipeId())
                    .orElseThrow();

            String ingredientName = ingredientMapper.normalizeIngredientName(row.ingredientName());
            Ingredient ingredient = ingredientRepository.findByName(ingredientName)
                    .orElseGet(() -> ingredientRepository.save(
                            Ingredient.create(row.ingredientName().trim())
                    ));

            MenuIngredient menuIngredient = ingredientMapper.toMenuIngredient(
                    row,
                    menu,
                    ingredient
            );

            menuIngredientRepository.save(menuIngredient);
        }
    }
}