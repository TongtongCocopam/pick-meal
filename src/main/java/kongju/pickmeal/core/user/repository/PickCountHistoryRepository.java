package kongju.pickmeal.core.user.repository;

import java.util.List;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.user.PickCountHistory;


public interface PickCountHistoryRepository {
    List<PickCountHistory> saveAll(List<PickCountHistory> pickCountHistories);

    PickCountHistory save(PickCountHistory pickCountHistory);

    void deleteAllByUser(User user);

    void deleteAllByUser_Family(Family family);
}
