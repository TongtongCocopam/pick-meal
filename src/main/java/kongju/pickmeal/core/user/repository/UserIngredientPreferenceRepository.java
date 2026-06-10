package kongju.pickmeal.core.user.repository;

import java.util.List;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserIngredientPreference;


public interface UserIngredientPreferenceRepository{
    void deleteAllByUser(User user);
    List<UserIngredientPreference> saveAll(List<UserIngredientPreference> userIngredientPreferenceList);
}
