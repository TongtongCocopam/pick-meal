package kongju.pickmeal.support.fixture;

import java.time.LocalDate;

import kongju.pickmeal.core.diet.Diet;
import kongju.pickmeal.core.menu.Menu;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.type.MealType;
import kongju.pickmeal.core.diet.DietGeneration;
import kongju.pickmeal.core.diet.type.DietMenuSource;


public class DietFixture {
    public static Diet diet(Family family, Menu menu, DietGeneration dietGeneration, DietMenuSource dietMenuSource) {
        return Diet.create(family, menu, LocalDate.now(), MealType.BREAKFAST, dietGeneration, dietMenuSource);
    }

}
