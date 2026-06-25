package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.diet.Diet;
import kongju.pickmeal.core.diet.repository.DietRepository;


@Repository
@RequiredArgsConstructor
public class DietRepositoryAdapter implements DietRepository {
    private final DietJpaRepository dietJpaRepository;

    @Override
    public List<Diet> saveAll(List<Diet> diets) {
        return dietJpaRepository.saveAll(diets);
    }
}
