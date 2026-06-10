package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.diet.Menu;
import kongju.pickmeal.core.diet.repository.MenuRepository;


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
}
