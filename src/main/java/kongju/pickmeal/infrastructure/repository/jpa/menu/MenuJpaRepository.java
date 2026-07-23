package kongju.pickmeal.infrastructure.repository.jpa.menu;

import java.util.Set;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.menu.type.MenuCategory;


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

    @Query("""
                    select m from Menu m
                    where(:category is null or m.category = :category)
                    and (:dishType is null or m.dishType = :dishType)
                    and m.id <> :menuId
                    and (:keyword is null or lower(m.menuName) like lower(concat('%', :keyword, '%') ) )
            """)
    Page<Menu> searchReplacementMenus(
            @Param("category") MenuCategory category,
            @Param("dishType") DishType dishType, Long menuId,
            @Param("keyword") String keyword, Pageable pageable);

    @Query("""
                select m
                from Menu m
                where m.category = :category
                  and m.dishType = :dishType
                  and m.id <> :currentMenuId
                  and not exists (
                      select 1
                      from MenuIngredient mi
                      where mi.menu = m
                        and mi.ingredient.id in :allergyIngredientIds
                  )
            """)
    List<Menu> findRecommendationCandidatesWithoutAllergy(
            @Param("category") MenuCategory category,
            @Param("dishType") DishType dishType,
            @Param("currentMenuId") Long currentMenuId,
            @Param("allergyIngredientIds") Set<Long> allergyIngredientIds);

    @Query("""
                select m
                from Menu m
                where m.category = :category
                  and m.dishType = :dishType
                  and m.id <> :currentMenuId
            """)
    List<Menu> findRecommendationCandidates(
            @Param("category") MenuCategory category,
            @Param("dishType") DishType dishType,
            @Param("currentMenuId") Long currentMenuId);

    @Query("""
            select m
            from Menu m
            where m.family = :family
            """)
    List<Menu> findAllByFamily(@Param("family") Family family);

    void deleteAll(List<Menu> menus);
}
