package kongju.pickmeal.infrastructure.repository.jpa.user;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserPickCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPickCountJpaRepository  extends JpaRepository<UserPickCount,Long> {
    Optional<UserPickCount> findByUser(User user);
}
