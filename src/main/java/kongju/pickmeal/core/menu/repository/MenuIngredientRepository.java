package kongju.pickmeal.core.menu.repository;

import java.util.List;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.menu.MenuIngredient;


public interface MenuIngredientRepository {
    boolean existsByMenuAndIngredient(Menu menu, Ingredient ingredient);

    MenuIngredient save(MenuIngredient menuIngredient);

    List<MenuIngredient> saveAll(List<MenuIngredient> menuIngredients);

    List<MenuIngredient> findAllByMenuWithIngredient(Menu menu);

    List<MenuIngredient> findAllByIngredientWithMenu(Ingredient ingredient);

    List<MenuIngredient> findAllByMenuInFetchIngredient(List<Menu> menus);

    void deleteAllByMenu(Menu menu);
}
