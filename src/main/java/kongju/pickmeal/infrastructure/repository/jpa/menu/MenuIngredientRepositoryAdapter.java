package kongju.pickmeal.infrastructure.repository.jpa.menu;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.menu.MenuIngredient;
import kongju.pickmeal.core.menu.repository.MenuIngredientRepository;


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

    @Override
    public List<MenuIngredient> findAllByMenuWithIngredient(Menu menu) {
        return menuIngredientJpaRepository.findAllByMenuWithIngredient(menu);
    }

    @Override
    public List<MenuIngredient> findAllByIngredientWithMenu(Ingredient ingredient) {
        return menuIngredientJpaRepository.findAllByIngredientWithMenu(ingredient);
    }

    @Override
    public List<MenuIngredient> findAllByMenuInFetchIngredient(List<Menu> menus) {
        return menuIngredientJpaRepository.findAllByMenuInFetchIngredient(menus);
    }

    @Override
    public void deleteAllByMenu(Menu menu) {
        menuIngredientJpaRepository.deleteAllByMenu(menu);
    }

    @Override
    public void flush() {
        menuIngredientJpaRepository.flush();
    }
}
