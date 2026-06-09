package kongju.pickmeal.infrastructure.external.recipe.service;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.core.diet.Ingredient;
import kongju.pickmeal.core.diet.MenuIngredient;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.diet.repository.MenuRepository;
import kongju.pickmeal.core.diet.repository.IngredientRepository;
import kongju.pickmeal.core.diet.repository.MenuIngredientRepository;
import kongju.pickmeal.infrastructure.external.recipe.RecipeApiClient;
import kongju.pickmeal.infrastructure.external.recipe.mapper.IngredientMapper;
import kongju.pickmeal.infrastructure.external.recipe.data.ingredient.RecipeIngredientRow;
import kongju.pickmeal.infrastructure.external.recipe.data.ingredient.RecipeIngredientApiResponse;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class IngredientImportService {

    private final MenuRepository menuRepository;
    private final RecipeApiClient recipeApiClient;
    private final IngredientMapper ingredientMapper;
    private final IngredientRepository ingredientRepository;
    private final MenuIngredientRepository menuIngredientRepository;

    /**
     * api로 재료 정보 가져와 db에 저장
     * @param startIdx 시작
     * @param endIdx 끝
     */
    public void importIngredients(int startIdx, int endIdx) {
        RecipeIngredientApiResponse response =
                recipeApiClient.fetchRecipeIngredients(startIdx, endIdx);

        if (response == null || response.grid() == null || response.grid().row() == null) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_EMPTY_RESPONSE);
        }

        for (RecipeIngredientRow row : response.grid().row()) {
            Optional<Menu> menuOptional = menuRepository.findByExternalRecipeId(row.recipeId());

            if (menuOptional.isEmpty()) {
                continue;
            }

            Menu menu = menuOptional.get();

            String ingredientName = ingredientMapper.normalizeIngredientName(row.ingredientName());
            Ingredient ingredient = ingredientRepository.findByName(ingredientName)
                    .orElseGet(() -> ingredientRepository.save(
                            Ingredient.create(row.ingredientName().trim())
                    ));

            if (menuIngredientRepository.existsByMenuAndIngredient(menu, ingredient)) {
                continue;
            }

            MenuIngredient menuIngredient = ingredientMapper.toMenuIngredient(
                    row,
                    menu,
                    ingredient
            );

            menuIngredientRepository.save(menuIngredient);
        }
    }
}