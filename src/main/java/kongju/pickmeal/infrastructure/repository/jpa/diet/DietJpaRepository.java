package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.List;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pickmeal.core.diet.Diet;
import kongju.pickmeal.core.family.Family;


public interface DietJpaRepository extends JpaRepository<Diet, Long> {
    @Query("""
            select d
            from Diet d
            join fetch d.menu
            where d.family = :family
            and d.mealDate between :startDate and :endDate
            order by d.mealDate asc
            """)
    List<Diet> findMonthlyDiets(
            @Param("family") Family family,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
                select d
                from Diet d
                join fetch d.menu
                where d.mealDate = :date
                and d.family = :family
            """)
    List<Diet> findAllFamilyAndMealDate(
            @Param("family") Family family,
            @Param("date") LocalDate date);

    void deleteAllByFamily(Family family);
}
