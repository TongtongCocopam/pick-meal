package kongju.pickmeal.application.diet;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.diet.DietGeneration;
import kongju.pickmeal.common.exception.ErrorCode;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.core.diet.repository.UserMenuPickRepository;
import kongju.pickmeal.core.diet.repository.DietGenerationRepository;


@Service
@RequiredArgsConstructor
public class DietGenerationFailureHandler {

    private final DietGenerationRepository dietGenerationRepository;
    private final UserMenuPickRepository userMenuPickRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailure(
            UUID generationId,
            List<Long> userMenuPickIds
    ) {
        DietGeneration generation = dietGenerationRepository.findById(generationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIET_GENERATION_NOT_FOUND));

        List<UserMenuPick> userMenuPicks = userMenuPickRepository.findAllByIdInForUpdate(userMenuPickIds);

        generation.failed();
        userMenuPicks.forEach(UserMenuPick::rollbackUse);
    }
}