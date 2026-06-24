package kongju.pickmeal.core.diet;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.type.MealType;
import kongju.pickmeal.core.common.BaseTimeEntity;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diet extends BaseTimeEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    private MealType mealType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dietGeneration_id")
    private DietGeneration dietGeneration;

    @Builder(access = AccessLevel.PRIVATE)
    private Diet(Family family, Menu menu, LocalDate mealDate, MealType mealType, DietGeneration dietGeneration) {
        this.family = family;
        this.menu = menu;
        this.mealDate = mealDate;
        this.mealType = mealType;
        this.dietGeneration = dietGeneration;
    }

    public static Diet create(
            Family family,
            Menu menu,
            LocalDate mealDate,
            MealType mealType,
            DietGeneration dietGeneration
    ) {
        return Diet.builder()
                .family(family)
                .menu(menu)
                .mealDate(mealDate)
                .mealType(mealType)
                .dietGeneration(dietGeneration)
                .build();
    }


}
