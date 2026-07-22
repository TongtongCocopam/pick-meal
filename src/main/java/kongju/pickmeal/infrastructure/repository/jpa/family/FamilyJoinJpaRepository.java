package kongju.pickmeal.infrastructure.repository.jpa.family;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import kongju.pickmeal.core.user.User;
import kongju.pickmeal.core.family.Family;
import kongju.pickmeal.core.family.ApplyStatus;
import kongju.pickmeal.core.family.FamilyJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;



public interface FamilyJoinJpaRepository extends JpaRepository<FamilyJoinRequest, Long> {

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

    void deleteByUser(User user);
}
