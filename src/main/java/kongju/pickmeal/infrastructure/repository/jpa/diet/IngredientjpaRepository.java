package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.Optional;

import kongju.pickmeal.core.menu.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;


public interface IngredientjpaRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByName(String name);

}
