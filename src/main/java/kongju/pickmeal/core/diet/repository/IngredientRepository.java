package kongju.pickmeal.core.diet.repository;

import java.util.Optional;

import kongju.pickmeal.core.diet.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;


public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByName(String name);
}
