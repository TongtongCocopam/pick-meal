package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.time.LocalDate;
import java.util.List;

import kongju.pickmeal.core.family.Family;
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

    @Override
    public List<Diet> findMonthlyDiets(Family family, LocalDate startDate, LocalDate endDate) {
        return dietJpaRepository.findMonthlyDiets(family, startDate, endDate);
    }
}
