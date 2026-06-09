package kongju.pickmeal.core.diet.repository;

import java.util.Optional;

import kongju.pickmeal.core.diet.Menu;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByExternalRecipeId(Long externalRecipeId);
    boolean existsByExternalRecipeId(Long externalRecipeId);
}
