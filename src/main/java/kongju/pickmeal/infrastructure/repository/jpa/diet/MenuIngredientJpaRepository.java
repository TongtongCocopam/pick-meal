package kongju.pickmeal.infrastructure.repository.jpa.diet;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.core.diet.Ingredient;
import kongju.pickmeal.core.diet.MenuIngredient;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MenuIngredientJpaRepository extends JpaRepository<MenuIngredient, Long> {
    boolean existsByMenuAndIngredient(Menu menu, Ingredient ingredient);

}
