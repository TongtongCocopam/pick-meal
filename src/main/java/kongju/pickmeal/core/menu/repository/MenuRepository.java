package kongju.pickmeal.core.menu.repository;

import java.util.Set;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;


public interface MenuRepository {
    Optional<Menu> findByExternalRecipeId(Long externalRecipeId);

    boolean existsByExternalRecipeId(Long externalRecipeId);

    List<Menu> saveAll(List<Menu> menus);

    Menu save(Menu menu);

    Page<Menu> searchByFilters(MenuCategory category, DishType dishType, String keyword, Pageable pageable);

    Optional<Menu> findById(Long id);

    List<Menu> findAll();

    Page<Menu> searchReplacementMenus(MenuCategory category, DishType dishType, Long menuId, String keyword, Pageable pageable);

    void delete(Menu menu);

    List<Menu> findRecommendationCandidatesWithoutAllergy(MenuCategory category, DishType dishType, Long currentMenuId, Set<Long> allergyIngredientIds);

    List<Menu> findRecommendationCandidates(MenuCategory category, DishType dishType, Long currentMenuId);
}
