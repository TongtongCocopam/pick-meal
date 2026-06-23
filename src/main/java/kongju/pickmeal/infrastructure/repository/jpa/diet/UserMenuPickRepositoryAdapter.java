package kongju.pickmeal.infrastructure.repository.jpa.diet;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.user.User;
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

    @Override
    public Optional<UserMenuPick> findByMenuIdAndUser(Long menuId, User user) {
        return userMenuPickJpaRepository.findByMenuIdAndUser(menuId, user);
    }

    @Override
    public void delete(UserMenuPick userMenuPick) {
        userMenuPickJpaRepository.delete(userMenuPick);
    }

    @Override
    public List<UserMenuPick> findAllByUserInFetchMenu(List<User> users) {
        return userMenuPickJpaRepository.findAllByUserInFetchMenu(users);
    }
}
