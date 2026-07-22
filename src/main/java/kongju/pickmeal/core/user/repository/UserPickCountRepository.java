package kongju.pickmeal.core.user.repository;

import java.util.Optional;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserPickCount;


public interface UserPickCountRepository {
    UserPickCount save(UserPickCount userPickCount);

    Optional<UserPickCount> findByUser(User user);

    Optional<UserPickCount> findByUserForUpdate(User user);

    void deleteByUser(User user);

}
