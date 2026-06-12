package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.menu.repository.IngredientRepository;


@Repository
@RequiredArgsConstructor
public class IngredientRepositoryAdapter implements IngredientRepository {
    private final IngredientjpaRepository ingredientjpaRepository;

    @Override
    public Optional<Ingredient> findByName(String name) {
        return ingredientjpaRepository.findByName(name);
    }

    @Override
    public Ingredient save(Ingredient ingredient) {
        return ingredientjpaRepository.save(ingredient);
    }

    @Override
    public Optional<Ingredient> findById(Long id) {
        return ingredientjpaRepository.findById(id);
    }
}
