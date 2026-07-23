package kongju.pickmeal.infrastructure.repository.jpa.menu;

import java.util.List;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.menu.MenuIngredient;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MenuIngredientJpaRepository extends JpaRepository<MenuIngredient, Long> {
    boolean existsByMenuAndIngredient(Menu menu, Ingredient ingredient);

    @Query("""
                    select mi
                    from MenuIngredient mi
                    Join fetch mi.ingredient
                    where mi.menu = : menu
            """)
    List<MenuIngredient> findAllByMenuWithIngredient(@Param("menu") Menu menu);

    @Query("""
                    select mi
                    from MenuIngredient mi
                    Join fetch mi.menu
                    where mi.ingredient = : ingredient
            """)
    List<MenuIngredient> findAllByIngredientWithMenu(@Param("ingredient") Ingredient ingredient);

    @Query("""
    select mi
    from MenuIngredient mi
    join fetch mi.menu
    join fetch mi.ingredient
    where mi.menu in :menus
""")
    List<MenuIngredient> findAllByMenuInFetchIngredient(@Param("menus") List<Menu> menus);

    void deleteAllByMenu(Menu menu);
}
