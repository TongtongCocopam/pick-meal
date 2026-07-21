package kongju.pickmeal.core.menu;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.common.BaseTimeEntity;
import kongju.pickmeal.core.menu.type.MenuCategory;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "menu",
        indexes = {
                @Index(name = "idx_menu_name", columnList = "menu_name"),
                @Index(name = "idx_menu_family_id", columnList = "family_id")
        }
)
public class Menu extends BaseTimeEntity {
    @Column(nullable = false, length = 100)
    private String menuName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DishType dishType;
    @Column(precision = 10, scale = 2)
    private BigDecimal kcal;
    @Column(precision = 10, scale = 2)
    private BigDecimal carbs;
    @Column(precision = 10, scale = 2)
    private BigDecimal protein;
    @Column(precision = 10, scale = 2)
    private BigDecimal fat;
    @Column(precision = 10, scale = 2)
    private BigDecimal sodium;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(name = "external_recipe_id", unique = true)
    private Long externalRecipeId;

    @Builder(access = AccessLevel.PRIVATE)
    private Menu(
            Long externalRecipeId, String menuName, MenuCategory category,
            DishType dishType, BigDecimal kcal, BigDecimal carbs,
            BigDecimal protein, BigDecimal fat, BigDecimal sodium,
            Family family) {
        this.externalRecipeId = externalRecipeId;
        this.menuName = menuName;
        this.category = category;
        this.dishType = dishType;
        this.kcal = kcal;
        this.carbs = carbs;
        this.protein = protein;
        this.fat = fat;
        this.sodium = sodium;
        this.family = family;
    }

    public static Menu createDefaultMenu(
            Long externalRecipeId,
            String menuName,
            MenuCategory category,
            DishType dishType,
            BigDecimal kcal,
            BigDecimal carbs,
            BigDecimal protein,
            BigDecimal fat,
            BigDecimal sodium
    ) {
        return Menu.builder()
                .externalRecipeId(externalRecipeId)
                .menuName(menuName)
                .category(category)
                .dishType(dishType)
                .kcal(kcal)
                .carbs(carbs)
                .protein(protein)
                .fat(fat)
                .sodium(sodium)
                .family(null)
                .build();
    }

    public static Menu createFamilyMenu(
            String menuName,
            MenuCategory category,
            DishType dishType,
            BigDecimal kcal,
            BigDecimal carbs,
            BigDecimal protein,
            BigDecimal fat,
            BigDecimal sodium,
            Family family
    ) {
        return Menu.builder()
                .externalRecipeId(null)
                .menuName(menuName)
                .category(category)
                .dishType(dishType)
                .kcal(kcal)
                .carbs(carbs)
                .protein(protein)
                .fat(fat)
                .sodium(sodium)
                .family(family)
                .build();
    }

    public void update(String menuName, MenuCategory category, DishType dishType,
                       BigDecimal kcal, BigDecimal carbs, BigDecimal protein,
                       BigDecimal fat, BigDecimal sodium) {
        this.menuName = menuName;
        this.category = category;
        this.dishType = dishType;
        this.kcal = kcal;
        this.carbs = carbs;
        this.protein = protein;
        this.fat=fat;
        this.sodium = sodium;
    }

}
