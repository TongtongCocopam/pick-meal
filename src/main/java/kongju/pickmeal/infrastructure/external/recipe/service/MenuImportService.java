package kongju.pickmeal.infrastructure.external.recipe.service;

import java.util.List;

import kongju.pickmeal.infrastructure.external.recipe.RecipeApiClient;
import kongju.pickmeal.infrastructure.external.recipe.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.core.diet.repository.MenuRepository;
import kongju.pickmeal.infrastructure.external.recipe.data.info.RecipeInfoApiResponse;


@Service
@Transactional
@RequiredArgsConstructor
public class MenuImportService {
    private final MenuMapper menuMapper;
    private final MenuRepository menuRepository;
    private final RecipeApiClient recipeApiClient;

    public void importMenus(int startIdx, int endIdx) {
        RecipeInfoApiResponse response =
                recipeApiClient.fetchRecipeInfos(startIdx, endIdx);

        List<Menu> menus = response.grid().row().stream()
                .map(menuMapper::toMenu)
                .toList();

        menuRepository.saveAll(menus);
    }

}
