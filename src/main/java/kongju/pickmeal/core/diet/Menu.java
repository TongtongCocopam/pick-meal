package kongju.pickmeal.core.diet;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.type.DishType;
import kongju.pickmeal.core.common.BaseTimeEntity;
import kongju.pickmeal.core.diet.type.MenuCategory;


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
    @Column(nullable = false)
    private String menuName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuCategory category;

    @Enumerated(EnumType.STRING)
    private DishType dishType;

    private BigDecimal kcal;
    private BigDecimal carbs;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal sodium;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(name = "external_recipe_id", unique = true)
    private Long externalRecipeId;

    @Builder(access = AccessLevel.PRIVATE)
    public Menu(
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
            Long externalRecipeId,
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
                .externalRecipeId(externalRecipeId)
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

}
