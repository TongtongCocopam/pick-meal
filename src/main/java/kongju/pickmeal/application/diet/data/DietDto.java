package kongju.pickmeal.application.diet.data;

import java.util.List;
import java.time.YearMonth;
import java.time.LocalDate;

import lombok.Builder;

import kongju.pickmeal.core.diet.type.MealType;
import kongju.pickmeal.core.menu.type.DishType;


public class DietDto {
    @Builder
    public record ListItemResponse(
            YearMonth month,
            int totalDays,
            List<DietResponse> diets,
            Boolean isGenerated
    ){
    }

    @Builder
    public record DietResponse(
            LocalDate date,
            List<MealResponse> meals
    ){}

    @Builder
    public record MealResponse(
            Long dietId,
            MealType mealType,
            DishType dishType,
            String menuName
    ){}
}
