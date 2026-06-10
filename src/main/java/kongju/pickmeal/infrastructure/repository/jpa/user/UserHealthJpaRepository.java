package kongju.pickmeal.infrastructure.repository.jpa.user;

import java.util.Optional;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserHealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserHealthJpaRepository extends JpaRepository<UserHealthProfile, Long> {
    Optional<UserHealthProfile> findByUser(User user);

}
