package kongju.pickmeal.infrastructure.repository.jpa.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserHealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserHealthJpaRepository extends JpaRepository<UserHealthProfile, Long> {
    Optional<UserHealthProfile> findByUser(User user);

    @Query("""
                    select uhp
                    from UserHealthProfile uhp
                    join fetch uhp.user
                    where uhp.user in :users
            """)
    List<UserHealthProfile> findAllByUserInFetchUser(List<User> users);

    void deleteByUser(User user);

}
