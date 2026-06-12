package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.Optional;

import kongju.pickmeal.core.menu.Menu;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MenuJpaRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByExternalRecipeId(Long externalRecipeId);
    boolean existsByExternalRecipeId(Long externalRecipeId);
}
