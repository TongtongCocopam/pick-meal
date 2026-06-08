package kongju.pickmeal.core.diet.repository;

import kongju.pickmeal.core.diet.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByExternalRecipeId(Long externalRecipeId);
}
