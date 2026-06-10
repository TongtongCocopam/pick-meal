package kongju.pickmeal.core.diet.repository;

import java.util.List;
import java.util.Optional;

import kongju.pickmeal.core.diet.Menu;


public interface MenuRepository{
    Optional<Menu> findByExternalRecipeId(Long externalRecipeId);
    boolean existsByExternalRecipeId(Long externalRecipeId);
    List<Menu> saveAll(List<Menu> menus);
}
