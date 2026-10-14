package kongju.pickmeal.infrastructure.external.ai;

import java.util.List;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import kongju.pickmeal.core.ai.DietAiGenerator;
import kongju.pickmeal.core.menu.type.DishType;
import kongju.pickmeal.core.ai.AiDietGenerateDto;


@Component
@Profile("load-test")
public class FakeAiDietGenerator implements DietAiGenerator {
    @Override
    public AiDietGenerateDto.Result generate(
            AiDietGenerateDto.Command command
    ) {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }

        int dayCount = Math.toIntExact(
                ChronoUnit.DAYS.between(
                        command.startDate(),
                        command.endDate()
                ) + 1
        );

        int soupCount = dayCount * command.dailyMealCount();
        int sideDishCount = soupCount * 2;

        List<Long> soupMenuIds = command.menuCandidates().stream()
                .filter(menu -> menu.dishType() == DishType.SOUP)
                .map(AiDietGenerateDto.MenuCandidate::menuId)
                .limit(soupCount)
                .toList();

        List<Long> sideDishMenuIds = command.menuCandidates().stream()
                .filter(menu -> menu.dishType() == DishType.SIDE_DISH)
                .map(AiDietGenerateDto.MenuCandidate::menuId)
                .limit(sideDishCount)
                .toList();

        return AiDietGenerateDto.Result.builder()
                .soupMenuIds(soupMenuIds)
                .sideDishMenuIds(sideDishMenuIds)
                .build();
    }
}