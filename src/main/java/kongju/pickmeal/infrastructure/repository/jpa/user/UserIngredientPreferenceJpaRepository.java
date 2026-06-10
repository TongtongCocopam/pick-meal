package kongju.pickmeal.infrastructure.repository.jpa.user;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserIngredientPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserIngredientPreferenceJpaRepository  extends JpaRepository<UserIngredientPreference, Long> {
    void deleteAllByUser(User user);
}
