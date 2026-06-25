package kongju.pickmeal.infrastructure.repository.jpa.diet;

import kongju.pickmeal.core.diet.Diet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DietJpaRepository extends JpaRepository<Diet, Long> {
    List<Diet> saveAll(List<Diet> diets);
}
