package kongju.pickmeal.core.user.repository;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserIngredientPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserIngredientPreferenceRepository extends JpaRepository<UserIngredientPreference, Long> {
    void deleteAllByUser(User user);
}
