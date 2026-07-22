package kongju.pickmeal.infrastructure.repository.jpa.user;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.PickCountHistory;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PickCountHistoryJpaRepository extends JpaRepository<PickCountHistory, Long> {
    void deleteAllByUser(User user);
}
