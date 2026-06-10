package kongju.pickmeal.core.user.repository;

import java.util.Optional;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserHealthProfile;


public interface UserHealthRepository {
    Optional<UserHealthProfile> findByUser(User user);
    UserHealthProfile save(UserHealthProfile userHealth);
}
