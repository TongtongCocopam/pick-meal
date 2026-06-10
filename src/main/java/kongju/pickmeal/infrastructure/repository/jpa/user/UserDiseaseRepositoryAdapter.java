package kongju.pickmeal.infrastructure.repository.jpa.user;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserDisease;
import kongju.pickmeal.core.user.repository.UserDiseaseRepository;


@Repository
@RequiredArgsConstructor
public class UserDiseaseRepositoryAdapter implements UserDiseaseRepository {
    private final UserDiseaseJpaRepository userDiseaseJpaRepository;

    @Override
    public void deleteAllByUser(User user) {
        userDiseaseJpaRepository.deleteAllByUser(user);
    }

    @Override
    public List<UserDisease> saveAll(List<UserDisease> userDiseases) {
        return userDiseaseJpaRepository.saveAll(userDiseases);
    }
}
