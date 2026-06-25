package kongju.pickmeal.core.diet.repository;

import kongju.pickmeal.core.diet.Diet;

import java.util.List;

public interface DietRepository {
    List<Diet> saveAll(List<Diet> diets);
}
