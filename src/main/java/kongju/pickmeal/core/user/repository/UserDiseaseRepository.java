package kongju.pickmeal.core.user.repository;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserDisease;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDiseaseRepository extends JpaRepository<UserDisease, Long> {
    void deleteAllByUser(User user);
}
