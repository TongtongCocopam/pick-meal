package kongju.pickmeal.core.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserIngredientPreferenceRepository extends JpaRepository<UserIngredientPreference, Long> {
    void deleteAllByUser(User user);
}
