package kongju.pickmeal.core.diet.repository;

import java.util.List;
import java.time.LocalDate;

import kongju.pickmeal.core.diet.Diet;
import kongju.pickmeal.core.family.Family;


public interface DietRepository {
    List<Diet> saveAll(List<Diet> diets);

    List<Diet> findMonthlyDiets(Family family, LocalDate startDate, LocalDate endDate);

    List<Diet> findAllFamilyAndMealDate(Family family, LocalDate date);
}
