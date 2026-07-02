package kongju.pickmeal.core.diet.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import kongju.pickmeal.core.diet.type.UserMenuPickStatus;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.diet.UserMenuPick;


public interface UserMenuPickRepository {
    List<UserMenuPick> saveAll(List<UserMenuPick> userMenuPicks);

    Optional<UserMenuPick> findByMenuIdAndUser(Long menuId, User user);

    void delete(UserMenuPick userMenuPick);

    List<UserMenuPick> findAllByUserInAndTargetMonthAndStatusFetchMenu(List<User> users, LocalDate targetMonth, UserMenuPickStatus status);
}
