package kongju.pickmeal.infrastructure.external.recipe.foodsafety;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;

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
import kongju.pickmeal.infrastructure.external.recipe.parser.ParsedIngredient;
import kongju.pickmeal.infrastructure.external.recipe.parser.IngredientMenuParser;
import kongju.pickmeal.infrastructure.external.recipe.foodsafety.data.FoodSafetyRecipeRow;
import kongju.pickmeal.infrastructure.external.recipe.foodsafety.data.FoodSafetyRecipeInfoResponse;


@Service
@Transactional
@RequiredArgsConstructor
public class FoodImportService {
    private final IngredientMenuParser ingredientMenuParser;

    private final FoodMapper foodMapper;

    private final MenuRepository menuRepository;
    private final IngredientRepository ingredientRepository;
    private final MenuIngredientRepository menuIngredientRepository;

    private final FoodSafetyRecipeApiClient foodSafetyRecipeApiClient;

    private FoodSafetyRecipeInfoResponse foodResponseGet(int startIdx, int endIdx) {
        FoodSafetyRecipeInfoResponse response =
                foodSafetyRecipeApiClient.fetchRecipe(startIdx, endIdx);

        if (response == null || response.info() == null || response.info().row() == null) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_EMPTY_RESPONSE);
        }

        return response;
    }

    public void importMenusIngredients(int startIdx, int endIdx) {
        FoodSafetyRecipeInfoResponse response = foodResponseGet(startIdx, endIdx);
        Set<Long> ingredientIds = new HashSet<>();

        for (FoodSafetyRecipeRow row : response.info().row()) {
            Menu menu = foodMapper.toMenu(row);
            Menu savedMenu = menuRepository.save(menu);

            List<MenuIngredient> menuIngredients = new ArrayList<>();

            List<ParsedIngredient> parseIngredients = ingredientMenuParser.extractIngredientParts(row.recipeParts());

            for (ParsedIngredient parseIngredient : parseIngredients) {
                Ingredient ingredient = ingredientRepository.findByName(parseIngredient.ingredientName())
                        .orElseGet(() -> ingredientRepository.save(
                                Ingredient.create(parseIngredient.ingredientName())
                        ));

                // 같은 메뉴 안에서 같은 ingredient 중복 방지
                if (!ingredientIds.add(ingredient.getId())) {
                    continue;
                }

                // DB에 이미 저장된 조합도 방지
                if (menuIngredientRepository.existsByMenuAndIngredient(savedMenu, ingredient)) {
                    continue;
                }

                MenuIngredient menuIngredient = foodMapper.toMenuIngredient(savedMenu, ingredient, parseIngredient.quantityText());
                menuIngredients.add(menuIngredient);
            }

            menuIngredientRepository.saveAll(menuIngredients);
        }

    }


}
