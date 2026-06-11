package kongju.pickmeal.core.diet.repository;

import java.util.List;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.core.diet.Ingredient;
import kongju.pickmeal.core.diet.MenuIngredient;


public interface MenuIngredientRepository {
    boolean existsByMenuAndIngredient(Menu menu, Ingredient ingredient);

    MenuIngredient save(MenuIngredient menuIngredient);

    List<MenuIngredient> saveAll(List<MenuIngredient> menuIngredients);
}
