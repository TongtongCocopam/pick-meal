package kongju.pickmeal.infrastructure.repository.jpa.diet;

import kongju.pickmeal.core.diet.Diet;
import kongju.pickmeal.core.family.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface DietJpaRepository extends JpaRepository<Diet, Long> {
    @Query("""
            select d
            from Diet d
            join fetch d.menu
            where d.family = :family
            and d.mealDate between :startDate and :endDate
            order by d.mealDate asc
            """)
    List<Diet> findMonthlyDiets(Family family, LocalDate startDate, LocalDate endDate);

    @Query("""
                select d
                from Diet d
                join fetch d.menu
                where d.mealDate = :date
                and d.family = :family
            """)
    List<Diet> findAllFamilyAndMealDate(Family family, LocalDate date);
}
