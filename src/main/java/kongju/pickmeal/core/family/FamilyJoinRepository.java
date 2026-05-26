package kongju.pickmeal.core.family;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pickmeal.core.user.User;


public interface FamilyJoinRepository extends JpaRepository<FamilyJoinRequest, Long> {

    @Query("SELECT COUNT(ja) > 0 " +
            "FROM FamilyJoinRequest ja WHERE ja.user = :user AND " +
            "ja.status =:status AND " +
            "ja.family =:family"
    )
    boolean checkPendingRequest(User user, Family family, ApplyStatus status);

    @Query("SELECT ja FROM FamilyJoinRequest ja " +
            "JOIN FETCH ja.user " +
            "WHERE ja.family = :family AND " +
            "ja.status = :status"
    )
    List<FamilyJoinRequest> findAllByFamilyAndStatus(Family family, ApplyStatus status);

    @Query("SELECT ja FROM FamilyJoinRequest ja " +
            "JOIN FETCH ja.user AND " +
            "WHERE ja.family = :family"
    )
    Optional<FamilyJoinRequest> findByIdAndFamily(Long id, Long family);
}
