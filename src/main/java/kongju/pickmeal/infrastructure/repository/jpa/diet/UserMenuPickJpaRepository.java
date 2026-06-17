package kongju.pickmeal.infrastructure.repository.jpa.diet;

import kongju.pickmeal.core.diet.UserMenuPick;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMenuPickJpaRepository extends JpaRepository<UserMenuPick, Long> {
}
