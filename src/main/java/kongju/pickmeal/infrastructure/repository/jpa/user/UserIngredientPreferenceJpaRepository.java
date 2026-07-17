package kongju.pickmeal.infrastructure.repository.jpa.user;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserIngredientPreference;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserIngredientPreferenceJpaRepository extends JpaRepository<UserIngredientPreference, Long> {
    void deleteAllByUser(User user);

    @Query("""
                select uip
                from UserIngredientPreference uip
                join fetch uip.ingredient
                where uip.user in :users
            """)
    List<UserIngredientPreference> findAllByUserInFetchIngredient(List<User> users);
}
