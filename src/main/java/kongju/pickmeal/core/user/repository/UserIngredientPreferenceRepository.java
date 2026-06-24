package kongju.pickmeal.core.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserIngredientPreference;


public interface UserIngredientPreferenceRepository {
    void deleteAllByUser(User user);

    List<UserIngredientPreference> saveAll(List<UserIngredientPreference> userIngredientPreferenceList);

    @Query("""
        select uip
        from UserIngredientPreference uip
        join fetch uip.ingredient
        where uip.user in :users
    """)
    List<UserIngredientPreference> findAllByUserInFetchIngredient(List<User> users);
}
