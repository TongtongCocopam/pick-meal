package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.DietGeneration;
import kongju.pickmeal.core.diet.type.DietGenerationStatus;
import kongju.pickmeal.core.diet.repository.DietGenerationRepository;


@Repository
@RequiredArgsConstructor
public class DietGenerationRepositoryAdapter implements DietGenerationRepository {
    private final DietGenerationJpaRepository  dietGenerationJpaRepository;
    @Override
    public DietGeneration save(DietGeneration dietGeneration) {
        return dietGenerationJpaRepository.save(dietGeneration);
    }

    @Override
    public Optional<DietGeneration> findById(UUID id) {
        return dietGenerationJpaRepository.findById(id);
    }

    @Override
    public boolean existsOverlappingGeneration(Family family, LocalDate startDate, LocalDate endDate, List<DietGenerationStatus> statuses) {
        return dietGenerationJpaRepository.existsOverlappingGeneration(family, startDate, endDate, statuses);
    }

    @Override
    public long countByFamilyAndTargetMonthAndStatusIn(Family family, LocalDate targetMonth, List<DietGenerationStatus> statuses) {
        return dietGenerationJpaRepository.countByFamilyAndTargetMonthAndStatusIn(family, targetMonth, statuses);
    }
}
