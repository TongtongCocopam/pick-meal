package kongju.pickmeal.infrastructure.repository.jpa.user;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.user.repository.UserRepository;


@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByLoginId(String loginId) {
        return userJpaRepository.findByLoginId(loginId);
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        return userJpaRepository.existsByLoginId(loginId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public List<User> findAllByFamily(Family family) {
        return userJpaRepository.findAllByFamily(family);
    }

    @Override
    public boolean existsByIdAndFamily(Long id, Family family) {
        return userJpaRepository.existsByIdAndFamily(id, family);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }
}
