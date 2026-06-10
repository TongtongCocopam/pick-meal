package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.Optional;

import kongju.pickmeal.core.diet.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



public interface MenuJpaRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByExternalRecipeId(Long externalRecipeId);
    boolean existsByExternalRecipeId(Long externalRecipeId);
}
