package kongju.pickmeal.infrastructure.repository.jpa.user;

import java.util.List;
import java.util.Optional;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserHealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface UserHealthJpaRepository extends JpaRepository<UserHealthProfile, Long> {
    Optional<UserHealthProfile> findByUser(User user);

    @Query("""
                    select uhp
                    from UserHealthProfile uhp
                    join fetch uhp.user
                    where uhp.user in :users
            """)
    List<UserHealthProfile> findAllByUserInFetchUser(List<User> users);

}
