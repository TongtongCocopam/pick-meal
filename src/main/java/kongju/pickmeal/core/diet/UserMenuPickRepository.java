package kongju.pickmeal.core.diet;

import java.util.List;
import java.util.Optional;

import kongju.pickmeal.core.user.User;


public interface UserMenuPickRepository {
    List<UserMenuPick> saveAll(List<UserMenuPick> userMenuPicks);

    Optional<UserMenuPick> findByMenuIdAndUser(Long menuId, User user);

    void delete(UserMenuPick userMenuPick);
}
