package kongju.pickmeal.core.diet.repository;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.core.diet.Ingredient;
import kongju.pickmeal.core.diet.MenuIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuIngredientRepository extends JpaRepository<MenuIngredient, Long> {
    boolean existsByMenuAndIngredient(Menu menu, Ingredient ingredient);
}
