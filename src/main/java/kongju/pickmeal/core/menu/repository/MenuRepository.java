package kongju.pickmeal.core.menu.repository;

import java.util.List;
import java.util.Optional;

import kongju.pickmeal.core.menu.Menu;


public interface MenuRepository{
    Optional<Menu> findByExternalRecipeId(Long externalRecipeId);
    boolean existsByExternalRecipeId(Long externalRecipeId);
    List<Menu> saveAll(List<Menu> menus);
    Menu save(Menu menu);
}
