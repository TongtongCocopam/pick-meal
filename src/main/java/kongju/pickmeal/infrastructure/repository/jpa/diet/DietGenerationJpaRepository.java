package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.DietGeneration;
import kongju.pickmeal.core.diet.type.DietGenerationStatus;


public interface DietGenerationJpaRepository extends JpaRepository<DietGeneration, UUID> {
    @Query("""
                select count(dg) > 0
                from DietGeneration dg
                where dg.family = :family
                  and dg.status in :statuses
                  and dg.startDate <= :endDate
                  and dg.endDate >= :startDate
            """)
    boolean existsOverlappingGeneration(
            @Param("family") Family family,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<DietGenerationStatus> statuses
    );

    long countByFamilyAndTargetMonthAndStatusIn(
            @Param("family") Family family,
            @Param("targetMonth") LocalDate targetMonth,
            @Param("status") List<DietGenerationStatus> status
    );

    void deleteAllByFamily(Family family);
}
