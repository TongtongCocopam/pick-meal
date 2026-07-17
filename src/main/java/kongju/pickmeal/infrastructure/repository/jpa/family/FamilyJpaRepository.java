package kongju.pickmeal.infrastructure.repository.jpa.family;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pickmeal.core.family.Family;


public interface FamilyJpaRepository extends JpaRepository<Family, Long> {
    boolean existsByInvitationCode(String invitationCode);

    Optional<Family> findByInvitationCode(String invitationCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Family f where f.id = :familyId")
    Optional<Family> findByIdForUpdate(Long familyId);
}
