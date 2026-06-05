package kongju.pickmeal.core.diet.repository;

import kongju.pickmeal.core.diet.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

}
