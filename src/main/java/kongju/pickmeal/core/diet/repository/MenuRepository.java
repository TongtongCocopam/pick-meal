package kongju.pickmeal.core.diet.repository;

import kongju.pickmeal.core.diet.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
