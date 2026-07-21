package kongju.pickmeal.infrastructure.repository.jpa.user;

import jakarta.persistence.LockModeType;
import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.user.UserPickCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserPickCountJpaRepository extends JpaRepository<UserPickCount, Long> {
    Optional<UserPickCount> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                select upc
                from UserPickCount upc
                where upc.user.id = :userId
            """)
    Optional<UserPickCount> findByUserForUpdate(User user);
}
