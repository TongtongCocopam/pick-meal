package kongju.pickmeal.core.diet.repository;

import kongju.pickmeal.core.diet.MenuIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuIngredientRepository extends JpaRepository<MenuIngredient, Long> {
}
