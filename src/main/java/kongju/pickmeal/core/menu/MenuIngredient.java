package kongju.pickmeal.core.menu;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import kongju.pickmeal.core.common.BaseEntity;
import kongju.pickmeal.core.menu.type.IngredientUnit;
import kongju.pickmeal.core.menu.type.IngredientType;


@Entity
@Getter
@Table(
        name = "menu_ingredients",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_menu_ingredient",
                        columnNames = {"menu_id", "ingredient_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuIngredient extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(name = "quantity_text", length = 100)
    private String quantityText;

    private Double quantity;

    @Enumerated(EnumType.STRING)
    private IngredientUnit unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "ingredient_type", length = 20)
    private IngredientType ingredientType;

    @Builder(access = AccessLevel.PRIVATE)
    public MenuIngredient(Menu menu, Ingredient ingredient, String quantityText,
                          Double quantity, IngredientUnit unit,  IngredientType ingredientType
    ) {
        this.menu = menu;
        this.ingredient = ingredient;
        this.quantityText = quantityText;
        this.quantity = quantity;
        this.unit = unit;
        this.ingredientType = ingredientType;
    }

    public static MenuIngredient create(
            Menu menu,
            Ingredient ingredient,
            String quantityText,
            Double quantity,
            IngredientUnit unit,
            IngredientType ingredientType
    ){
        return MenuIngredient.builder()
                .menu(menu)
                .ingredient(ingredient)
                .quantityText(quantityText)
                .quantity(quantity)
                .unit(unit)
                .ingredientType(ingredientType)
                .build();
    }


}
