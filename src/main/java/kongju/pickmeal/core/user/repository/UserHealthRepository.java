package kongju.pickmeal.core.user.repository;

import java.util.Optional;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserHealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserHealthRepository extends JpaRepository<UserHealthProfile, Long> {
    Optional<UserHealthProfile> findByUser(User user);
}
