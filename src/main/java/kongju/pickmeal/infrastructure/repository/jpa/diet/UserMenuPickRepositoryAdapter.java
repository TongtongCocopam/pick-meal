package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.diet.UserMenuPick;
import kongju.pickmeal.core.diet.UserMenuPickRepository;


@Repository
@RequiredArgsConstructor
public class UserMenuPickRepositoryAdapter implements UserMenuPickRepository {
    private final UserMenuPickJpaRepository userMenuPickJpaRepository;

    @Override
    public List<UserMenuPick> saveAll(List<UserMenuPick> userMenuPicks) {
        return userMenuPickJpaRepository.saveAll(userMenuPicks);
    }
}
