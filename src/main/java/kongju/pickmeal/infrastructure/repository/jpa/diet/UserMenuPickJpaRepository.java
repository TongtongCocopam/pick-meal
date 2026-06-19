package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.diet.UserMenuPick;


public interface UserMenuPickJpaRepository extends JpaRepository<UserMenuPick, Long> {
    Optional<UserMenuPick> findByMenuIdAndUser(Long menuId, User user);
}
