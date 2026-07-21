package kongju.pickmeal.infrastructure.repository.jpa.user;

import kongju.pickmeal.core.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.user.UserPickCount;
import kongju.pickmeal.core.user.repository.UserPickCountRepository;

import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class UserPickCountRepositoryAdapter implements UserPickCountRepository {
    private final UserPickCountJpaRepository userPickCountJpaRepository;

    @Override
    public UserPickCount save(UserPickCount userPickCount) {
        return userPickCountJpaRepository.save(userPickCount);
    }

    @Override
    public Optional<UserPickCount> findByUser(User user) {
        return userPickCountJpaRepository.findByUser(user);
    }

    @Override
    public Optional<UserPickCount> findByUserForUpdate(User user) {
        return userPickCountJpaRepository.findByUserForUpdate(user);
    }
}
