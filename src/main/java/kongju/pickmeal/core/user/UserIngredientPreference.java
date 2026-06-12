package kongju.pickmeal.core.user;

import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import kongju.pickmeal.core.menu.Ingredient;
import kongju.pickmeal.core.common.BaseTimeEntity;
import kongju.pickmeal.core.user.type.FoodPreferenceType;

@Entity
@Table(
        name = "user_ingredient_preference",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_ingredient_preference_user_ingredient",
                        columnNames = {"user_id", "ingredient_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserIngredientPreference extends BaseTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodPreferenceType preference;

    @Builder
    public UserIngredientPreference(User user, Ingredient ingredient, FoodPreferenceType preference) {
        this.user = user;
        this.ingredient = ingredient;
        this.preference = preference;
    }
}
