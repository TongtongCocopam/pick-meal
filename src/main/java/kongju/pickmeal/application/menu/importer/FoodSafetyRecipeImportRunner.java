package kongju.pickmeal.application.menu.importer;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;

import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.infrastructure.external.recipe.foodsafety.FoodImportService;


@Slf4j
@Profile("food-safety-import")
@Component
@RequiredArgsConstructor
public class FoodSafetyRecipeImportRunner implements CommandLineRunner {
    private final FoodImportService foodImportService;

    @Override
    public void run(String... args) {
        int totalCount = 1146;
        int pageSize = 100;

        for (int start = 1; start <= totalCount; start += pageSize) {
            int end = Math.min(start + pageSize - 1, totalCount);
            try{
                foodImportService.importMenusIngredients(start, end);
            }catch(Exception e){
                log.error("food-safety-import 실패 : start={}, end={}", start, end, e);
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
            }

        }
    }
}
