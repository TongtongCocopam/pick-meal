package kongju.pickmeal.core.menu.repository;

import java.util.Optional;

import kongju.pickmeal.core.menu.Ingredient;


public interface IngredientRepository {
    Optional<Ingredient> findByName(String name);

    Ingredient save(Ingredient ingredient);

    Optional<Ingredient> findById(Long id);
}
