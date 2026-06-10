package kongju.pickmeal.core.diet.repository;

import java.util.Optional;

import kongju.pickmeal.core.diet.Ingredient;


public interface IngredientRepository {
    Optional<Ingredient> findByName(String name);

    Ingredient save(Ingredient ingredient);

    Optional<Ingredient> findById(Long id);
}
