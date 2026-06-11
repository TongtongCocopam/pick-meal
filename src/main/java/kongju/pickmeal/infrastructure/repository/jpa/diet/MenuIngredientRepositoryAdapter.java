package kongju.pickmeal.infrastructure.repository.jpa.diet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.core.diet.Ingredient;
import kongju.pickmeal.core.diet.MenuIngredient;
import kongju.pickmeal.core.diet.repository.MenuIngredientRepository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class MenuIngredientRepositoryAdapter implements MenuIngredientRepository {
    private final MenuIngredientJpaRepository menuIngredientJpaRepository;

    @Override
    public boolean existsByMenuAndIngredient(Menu menu, Ingredient ingredient) {
        return menuIngredientJpaRepository.existsByMenuAndIngredient(menu, ingredient);
    }

    @Override
    public MenuIngredient save(MenuIngredient menuIngredient) {
        return menuIngredientJpaRepository.save(menuIngredient);
    }

    @Override
    public List<MenuIngredient> saveAll(List<MenuIngredient> menuIngredients) {
        return menuIngredientJpaRepository.saveAll(menuIngredients);
    }
}
