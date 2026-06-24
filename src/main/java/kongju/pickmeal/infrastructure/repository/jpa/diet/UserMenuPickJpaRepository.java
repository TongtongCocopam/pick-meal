package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.diet.UserMenuPick;
import org.springframework.data.jpa.repository.Query;


public interface UserMenuPickJpaRepository extends JpaRepository<UserMenuPick, Long> {
    Optional<UserMenuPick> findByMenuIdAndUser(Long menuId, User user);

    @Query("""
                    select ump
                    from UserMenuPick ump
                    join fetch ump.menu
                    where ump.user in :users
            """)
    List<UserMenuPick> findAllByUserInFetchMenu(List<User> users);
}
