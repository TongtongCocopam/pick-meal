package kongju.pickmeal.core.user.repository;

import java.util.List;
import java.util.Optional;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserDisease;


public interface UserDiseaseRepository {
    void deleteAllByUser(User user);

    List<UserDisease> saveAll(List<UserDisease> userDiseases);

    List<UserDisease> findAllByUserIn(List<User> user);
}
