package kongju.pickmeal.core.diet.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.diet.type.UserMenuPickStatus;


public interface UserMenuPickRepository {
    List<UserMenuPick> saveAll(List<UserMenuPick> userMenuPicks);

    Optional<UserMenuPick> findByMenuIdAndUser(Long menuId, User user);

    void delete(UserMenuPick userMenuPick);

    List<UserMenuPick> findAllByIdInFetchMenu(List<Long> ids);

    List<UserMenuPick> findAllPendingForUpdate(Family family,LocalDate targetMonthDate, UserMenuPickStatus status);

    List<UserMenuPick> findAllByIdInForUpdate(List<Long> ids);
}
