package kongju.pickmeal.infrastructure.repository.jpa.menu;

import java.util.List;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.menu.MenuIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface MenuIngredientJpaRepository extends JpaRepository<MenuIngredient, Long> {
    boolean existsByMenuAndIngredient(Menu menu, Ingredient ingredient);

    @Query("""
                    select mi
                    from MenuIngredient mi
                    Join fetch mi.ingredient
                    where mi.menu = : menu
            """)
    List<MenuIngredient> findAllByMenuWithIngredient(Menu menu);

}
