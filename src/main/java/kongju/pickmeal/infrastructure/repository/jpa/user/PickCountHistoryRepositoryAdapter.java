package kongju.pickmeal.infrastructure.repository.jpa.user;

import kongju.pickmeal.core.user.PickCountHistory;
import kongju.pickmeal.core.user.repository.PickCountHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PickCountHistoryRepositoryAdapter implements PickCountHistoryRepository {
    private final PickCountHistoryJpaRepository pickCountHistoryJpaRepository;

    @Override
    public List<PickCountHistory> saveAll(List<PickCountHistory> pickCountHistories) {
        return pickCountHistoryJpaRepository.saveAll(pickCountHistories);
    }

    @Override
    public PickCountHistory save(PickCountHistory pickCountHistory) {
        return pickCountHistoryJpaRepository.save(pickCountHistory);
    }
}
