package kongju.pickmeal.infrastructure.repository.jpa.user;

import java.util.List;
import java.util.Optional;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.family.Family;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    List<User> findAllByFamily(Family family);

    boolean existsByIdAndFamily(Long id, Family family);
}
