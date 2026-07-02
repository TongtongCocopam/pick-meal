package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.diet.type.UserMenuPickStatus;


public interface UserMenuPickJpaRepository extends JpaRepository<UserMenuPick, Long> {
    Optional<UserMenuPick> findByMenuIdAndUser(Long menuId, User user);

    @Query("""
                    select ump
                    from UserMenuPick ump
                    join fetch ump.menu
                    where ump.user in :users
                    and ump.targetMonth = :targetMonth
                    and ump.status = :status
            """)
    List<UserMenuPick> findAllByUserInAndTargetMonthAndStatusFetchMenu(List<User> users, LocalDate targetMonth, UserMenuPickStatus status);
}
