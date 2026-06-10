package kongju.pickmeal.infrastructure.repository.jpa.user;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserIngredientPreference;
import kongju.pickmeal.core.user.repository.UserIngredientPreferenceRepository;


@Repository
@RequiredArgsConstructor
public class UserIngredientPreferenceRepositoryAdapter implements UserIngredientPreferenceRepository {
    private final UserIngredientPreferenceJpaRepository userIngredientPreferenceJpaRepository;

    @Override
    public void deleteAllByUser(User user) {
        userIngredientPreferenceJpaRepository.deleteAllByUser(user);
    }

    @Override
    public List<UserIngredientPreference> saveAll(List<UserIngredientPreference> userIngredientPreferenceList) {
        return userIngredientPreferenceJpaRepository.saveAll(userIngredientPreferenceList);
    }
}
