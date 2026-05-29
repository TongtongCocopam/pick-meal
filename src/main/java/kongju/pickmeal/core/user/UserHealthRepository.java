package kongju.pickmeal.core.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UserHealthRepository extends JpaRepository<UserHealthProfile, Long> {
    Optional<UserHealthProfile> findByUser(User user);
}
