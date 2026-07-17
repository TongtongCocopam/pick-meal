package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.diet.Diet;
import kongju.pickmeal.core.family.Family;
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

    @Override
    public List<Diet> findAllFamilyAndMealDate(Family family, LocalDate date) {
        return dietJpaRepository.findAllFamilyAndMealDate(family, date);
    }

    @Override
    public Optional<Diet> findById(Long id) {
        return dietJpaRepository.findById(id);
    }
}
