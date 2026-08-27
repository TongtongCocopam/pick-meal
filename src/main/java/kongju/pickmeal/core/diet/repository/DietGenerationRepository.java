package kongju.pickmeal.core.diet.repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.LocalDate;

import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.DietGeneration;
import kongju.pickmeal.core.diet.type.DietGenerationStatus;


public interface DietGenerationRepository {
    DietGeneration save(DietGeneration dietGeneration);

    Optional<DietGeneration> findById(UUID id);

    boolean existsOverlappingGeneration(Family family, LocalDate startDate, LocalDate endDate, List<DietGenerationStatus> statuses);

    long countByFamilyAndTargetMonthAndStatusIn(Family family, LocalDate targetMonth, List<DietGenerationStatus> statuses);

    void deleteAllByFamily(Family family);
}
