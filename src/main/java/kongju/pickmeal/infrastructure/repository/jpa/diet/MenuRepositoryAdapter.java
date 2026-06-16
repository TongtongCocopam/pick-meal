package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;
import kongju.pickmeal.core.menu.repository.MenuRepository;


@Repository
@RequiredArgsConstructor
public class MenuRepositoryAdapter implements MenuRepository {
    private final MenuJpaRepository menuJpaRepository;

    @Override
    public Optional<Menu> findByExternalRecipeId(Long externalRecipeId) {
        return menuJpaRepository.findByExternalRecipeId(externalRecipeId);
    }

    @Override
    public boolean existsByExternalRecipeId(Long externalRecipeId) {
        return menuJpaRepository.existsByExternalRecipeId(externalRecipeId);
    }

    @Override
    public List<Menu> saveAll(List<Menu> menus) {
        return menuJpaRepository.saveAll(menus);
    }

    @Override
    public Menu save(Menu menu) {
        return menuJpaRepository.save(menu);
    }

    @Override
    public Page<Menu> searchByFilters(MenuCategory category, DishType dishType, String keyword, Pageable pageable) {
        return menuJpaRepository.searchByFilters(category, dishType, keyword, pageable);
    }

    @Override
    public Optional<Menu> findById(Long id) {
        return menuJpaRepository.findById(id);
    }
}
