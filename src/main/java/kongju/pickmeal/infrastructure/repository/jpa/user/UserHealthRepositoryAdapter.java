package kongju.pickmeal.infrastructure.repository.jpa.user;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserHealthProfile;
import kongju.pickmeal.core.user.repository.UserHealthRepository;


@Repository
@RequiredArgsConstructor
public class UserHealthRepositoryAdapter implements UserHealthRepository {
    private final UserHealthJpaRepository userHealthJpaRepository;

    @Override
    public Optional<UserHealthProfile> findByUser(User user) {
        return userHealthJpaRepository.findByUser(user);
    }

    @Override
    public UserHealthProfile save(UserHealthProfile userHealth) {
        return userHealthJpaRepository.save(userHealth);
    }

    @Override
    public List<UserHealthProfile> findAllByUserInFetchUser(List<User> users) {
        return userHealthJpaRepository.findAllByUserInFetchUser(users);
    }
}
