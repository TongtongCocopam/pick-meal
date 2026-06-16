package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;
import org.springframework.data.repository.query.Param;


public interface MenuJpaRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByExternalRecipeId(Long externalRecipeId);

    boolean existsByExternalRecipeId(Long externalRecipeId);

    @Query("""
                    select m from Menu m
                    where(:category is null or m.category = :category)
                    and (:dishType is null or m.dishType = :dishType)
                    and (:keyword is null or lower(m.menuName) like lower(concat('%', :keyword, '%') ) )
            """)
    Page<Menu> searchByFilters(
            @Param("category") MenuCategory category,
            @Param("dishType") DishType dishType,
            @Param("keyword") String keyword, Pageable pageable);
}
