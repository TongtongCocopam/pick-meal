package kongju.pickmeal.infrastructure.repository.jpa.user;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserDisease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDiseaseJpaRepository extends JpaRepository<UserDisease, Long> {
    void deleteAllByUser(User user);

    List<UserDisease> findAllByUserIn(List<User> user);
}
